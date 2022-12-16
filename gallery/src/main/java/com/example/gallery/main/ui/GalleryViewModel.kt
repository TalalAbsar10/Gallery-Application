package com.example.gallery.main.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.gallery.R
import com.example.gallery.main.gallery.BucketRecyclerViewItemMode
import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.main.gallery.UNLIMITED_SELECT
import com.example.gallery.utils.BaseViewModel
import com.example.gallery.utils.MediaStoreObserver
import com.example.gallery.utils.SingleLiveEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

internal class GalleryViewModel(
    private val galleryOptions: GalleryOptions,
    private val mediaObserver: MediaStoreObserver
) : BaseViewModel() {

    private val _currentFragmentLiveData = SingleLiveEvent<GalleryView?>()
    val currentFragmentLiveData: LiveData<GalleryView?> = _currentFragmentLiveData

    val userSelectedMedias: Boolean
        get() = mediaSelectionTracker.isNotEmpty()

    val mediaSelectionTracker = mutableListOf<String>()
    var totalMediaCount = 0
        set(value) {
            field = if (galleryOptions.maxSelectableMedia == UNLIMITED_SELECT) value else galleryOptions.maxSelectableMedia
        }
        get() = if (galleryOptions.maxSelectableMedia == UNLIMITED_SELECT) field else galleryOptions.maxSelectableMedia

    private val captionEnabledMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val captionEnabledStateFlow: StateFlow<Boolean?> = captionEnabledMutableStateFlow
    private val sendActionEnabledMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val sendActionEnabledStateFlow: StateFlow<Boolean?> = sendActionEnabledMutableStateFlow
    private val storagePermissionGrantedStateMutableStateFlow = MutableStateFlow<Boolean?>(null)
    val storagePermissionGrantedStateFlow: StateFlow<Boolean?> = storagePermissionGrantedStateMutableStateFlow
    private val bucketRecyclerViewMode = MutableLiveData<BucketRecyclerViewItemMode>()
    val bucketRecycleViewModeLiveData: LiveData<BucketRecyclerViewItemMode> = bucketRecyclerViewMode
    private val mediaCountMutableStateFlow = MutableStateFlow(MediaCountModel(0, 0))
    val mediaCountStateFlow: StateFlow<MediaCountModel> = mediaCountMutableStateFlow
    private val allMediaDeselectedMutableStateFlow = MutableStateFlow(false)
    val allMediaDeselectedStateFlow: Flow<Boolean> = allMediaDeselectedMutableStateFlow
    private var captionOrSendActionState = true
    private var cameraTemporaryFilePath: String? = null

    private val _resultSingleLiveEvent = SingleLiveEvent<Array<String>>()
    val resultSingleLiveEvent: LiveData<Array<String>> = _resultSingleLiveEvent

    private val _showErrorSingleLiveEvent = SingleLiveEvent<Int>()
    val showErrorSingleLiveEvent: LiveData<Int> = _showErrorSingleLiveEvent

    private val externalStorageMediaObserver by lazy {
        Observer<Uri?> {
            validateSelections()
        }
    }

    init {
        _currentFragmentLiveData.value = GalleryView.BucketList
        if (mediaSelectionTracker.isNotEmpty() && captionOrSendActionState) {
            if (galleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = true
            else
                sendActionEnabledMutableStateFlow.value = true
        }

        if (galleryOptions.mediaObserverEnabled) {
            mediaObserver.externalStorageChangeState.observeForever(externalStorageMediaObserver)
        }
    }

    fun changeRecyclerViewItemMode(bucketRecyclerViewItemMode: BucketRecyclerViewItemMode) {
        bucketRecyclerViewMode.value = bucketRecyclerViewItemMode
    }

    fun openBucketWithId(it: Long, bucketName: String) {
        _currentFragmentLiveData.value = GalleryView.BucketContent(it, bucketName)

    }

    fun storagePermissionGranted() {
        storagePermissionGrantedStateMutableStateFlow.value = true
    }

    fun requestDeselectingMedia(path: String): Boolean {
        if (!mediaSelectionTracker.contains(path)) return false
        return mediaSelectionTracker.remove(path).also {
            if (it && mediaSelectionTracker.isEmpty() && captionOrSendActionState) {
                if (galleryOptions.captionEnabledOptions.enabled)
                    captionEnabledMutableStateFlow.value = false
                else
                    sendActionEnabledMutableStateFlow.value = false
            }

            if (galleryOptions.mediaCountEnabled && it)
                mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        }
    }

    fun requestSelectingMedia(path: String): Boolean {
        if (galleryOptions.maxSelectableMedia != UNLIMITED_SELECT && galleryOptions.maxSelectableMedia == mediaSelectionTracker.size) {
            _showErrorSingleLiveEvent.value = R.string.gallery_error_max_selectable
            return false
        }

        return mediaSelectionTracker.add(path).also {
            if (it && mediaSelectionTracker.size == 1 && captionOrSendActionState) {
                if (galleryOptions.captionEnabledOptions.enabled)
                    captionEnabledMutableStateFlow.value = true
                else
                    sendActionEnabledMutableStateFlow.value = true
            }

            if (galleryOptions.mediaCountEnabled && it)
                mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
        }
    }

    fun deselectAllSelections() {
        mediaSelectionTracker.clear()
        allMediaDeselectedMutableStateFlow.value = true
        allMediaDeselectedMutableStateFlow.value = false

        if (captionOrSendActionState) {
            if (galleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = false
            else
                sendActionEnabledMutableStateFlow.value = false
        }

        if (galleryOptions.mediaCountEnabled)
            mediaCountMutableStateFlow.value = MediaCountModel(0, totalMediaCount)
    }

    fun isPhotoSelected(path: String): Boolean = mediaSelectionTracker.contains(path)

    fun showSendOrCaptionLayout() {
        captionOrSendActionState = true
        if (mediaSelectionTracker.isNotEmpty()) {
            if (galleryOptions.captionEnabledOptions.enabled)
                captionEnabledMutableStateFlow.value = true
            else
                sendActionEnabledMutableStateFlow.value = true
        }
    }

    fun hideSendOrCaptionLayout() {
        captionOrSendActionState = false
        if (galleryOptions.captionEnabledOptions.enabled)
            captionEnabledMutableStateFlow.value = false
        else
            sendActionEnabledMutableStateFlow.value = false
    }

    fun clearCameraPhotoFileAddress() {
        cameraTemporaryFilePath = null
    }

    fun prepareSelectedResults() {
        _resultSingleLiveEvent.value = mediaSelectionTracker.toTypedArray()
    }

    fun setCameraPhotoFileAddress(path: String) {
        cameraTemporaryFilePath = path
    }

    private fun validateSelections() {
        try {
            val validatedList = mediaSelectionTracker.filter { File(it).exists() }
            mediaSelectionTracker.clear()
            mediaSelectionTracker.addAll(validatedList)
            mediaCountMutableStateFlow.value = MediaCountModel(mediaSelectionTracker.size, totalMediaCount)
            if (mediaSelectionTracker.isEmpty()) {
                if (galleryOptions.captionEnabledOptions.enabled)
                    captionEnabledMutableStateFlow.value = false
                else
                    sendActionEnabledMutableStateFlow.value = false
            }
        } catch (t: SecurityException) {
            storagePermissionGrantedStateMutableStateFlow.value = false
            t.printStackTrace()
        }
    }

    fun prepareCameraResultWithSelectedResults() {
        if (cameraTemporaryFilePath != null) {
            _resultSingleLiveEvent.value = mediaSelectionTracker.apply { add(cameraTemporaryFilePath!!) }.toTypedArray()
        }
    }

    fun clearLatestValueOfCurrentFragmentLiveData() {
        _currentFragmentLiveData.value = null
    }

    override fun onCleared() {
        mediaObserver.externalStorageChangeState.removeObserver(externalStorageMediaObserver)
        super.onCleared()
    }
}