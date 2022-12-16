package com.example.gallery.buckets.bucketList

import android.net.Uri
import android.util.Log
import androidx.lifecycle.Observer
import com.example.gallery.models.BucketType
import com.example.gallery.models.MediaBucket
import com.example.gallery.repo.AbstractMediaBucketProvider
import com.example.gallery.utils.BaseViewModel
import com.example.gallery.utils.GALLERY_LOG_TAG
import com.example.gallery.utils.MediaStoreObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class BucketListViewModel constructor(
    mediaObserverEnabled: Boolean,
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType: BucketType = BucketType.VIDEO_PHOTO_BUCKETS,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mediaStoreObserver: MediaStoreObserver
) : BaseViewModel() {

    private val bucketsMutableStateFlow = MutableStateFlow<List<MediaBucket>>(listOf())
    private val allMediaCountChangedStateFlow = MutableStateFlow(0)
    val allMediaCountChanged: StateFlow<Int> = allMediaCountChangedStateFlow
    val bucketsStateFlow: StateFlow<List<MediaBucket>> = this.bucketsMutableStateFlow
    private val loadingMutableStateFlow = MutableStateFlow<LoadingViewState?>(null)
    val loadingViewStateFlow: StateFlow<LoadingViewState?> = loadingMutableStateFlow

    private val externalStorageMediaObserver by lazy {
        Observer<Uri?> {
            getBuckets(true)
        }
    }

    init {
        if (mediaObserverEnabled) {
            mediaStoreObserver.externalStorageChangeState.observeForever(externalStorageMediaObserver)
        }
    }

    fun getBucketNameById(id: Long): String = bucketsStateFlow.value.firstOrNull {
        id == it.id
    }?.displayName ?: "Unknown"

    fun getBuckets(refresh: Boolean = false) {
        if (!refresh && bucketsStateFlow.value.isNotEmpty() || loadingViewStateFlow.value == LoadingViewState.ShowLoading) return
        loadingMutableStateFlow.value = LoadingViewState.ShowLoading
        viewModelScope.launch(ioDispatcher) {
            try {
                abstractMediaBucketProvider.getMediaBuckets(bucketType).also { buckets ->
                    viewModelScope.launch(Dispatchers.Main) {
                        loadingMutableStateFlow.value = LoadingViewState.HideLoading
                        bucketsMutableStateFlow.value = buckets
                        allMediaCountChangedStateFlow.value = buckets.firstOrNull()?.mediaCount ?: 0
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                Log.e(GALLERY_LOG_TAG, "error while fetching buckets.(${t.message})")
                viewModelScope.launch(Dispatchers.Main) {
                    loadingMutableStateFlow.value = LoadingViewState.HideLoading
                    loadingMutableStateFlow.value = LoadingViewState.Error
                }
            }
        }
    }

    fun retry() {
        getBuckets(true)
    }

    override fun onCleared() {
        mediaStoreObserver.externalStorageChangeState.removeObserver(externalStorageMediaObserver)
        super.onCleared()
    }
}

