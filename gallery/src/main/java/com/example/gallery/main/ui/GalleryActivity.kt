package com.example.gallery.main.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gallery.R
import com.example.gallery.buckets.bucketList.BucketListFragment
import com.example.gallery.main.di.GalleryActivityComponentHolder
import com.example.gallery.main.di.GalleryCoreComponentHolder
import com.example.gallery.main.gallery.BucketRecyclerViewItemMode
import com.example.gallery.utils.*
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.caption_layout.*
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.io.File

internal class GalleryActivity : AppCompatActivity(), GalleryToolbarVisibilityController {

    private lateinit var galleryViewModel: GalleryViewModel
    private val galleryOptions by lazy { GalleryActivityComponentHolder.createOrGetComponent(this).provideGalleryOptions() }
    private var frameLayoutSendMediaAnimationPostRunnable: Runnable? = null
    private var checkSharedStoragePermissionInOnStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            GalleryCoreComponentHolder.getOrThrow()
        } catch (t: Throwable) {
            finish()
        }
        GalleryActivityComponentHolder.createOrGetComponent(this)
        requestedOrientation = galleryOptions.orientationMode
        setTheme(galleryOptions.themeResId)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        initViewModel()
        initialize()
        initView()
    }

    override fun onStart() {
        super.onStart()
        if (checkSharedStoragePermissionInOnStart) {
            checkSharedStoragePermission()
        }
    }

    private fun initialize() {
        if (!galleryOptions.grantExternalStoragePermission) {
            galleryViewModel.storagePermissionGranted()
        } else {
            permissionChecker(Manifest.permission.WRITE_EXTERNAL_STORAGE, granted = {
                checkSharedStoragePermission()
            }, denied = {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_EXTERNAL_REQUEST_CODE)
            })
        }
    }

    private fun checkSharedStoragePermission() {
        if (galleryOptions.grantSharedStorePermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestSharedStoragePermission(granted = {
                galleryViewModel.storagePermissionGranted()
                checkSharedStoragePermissionInOnStart = false
            }, denied = {
                Toast.makeText(this, R.string.shared_storage_denied_text, Toast.LENGTH_SHORT).show()
                checkSharedStoragePermissionInOnStart = true
            })
        } else {
            galleryViewModel.storagePermissionGranted()
        }
    }

    private fun initViewModel() {
        galleryViewModel = ViewModelProvider(
            this,
            GalleryActivityComponentHolder.createOrGetComponent(this).provideGalleryViewModelFactory()
        )[GalleryViewModel::class.java]

        galleryViewModel.apply {
            showErrorSingleLiveEvent.observe(this@GalleryActivity, Observer(this@GalleryActivity::showError))
            resultSingleLiveEvent.observe(this@GalleryActivity, Observer(this@GalleryActivity::handleGalleryResults))

            lifecycleScope.launch {
                launch {
                    sendActionEnabledStateFlow.mapNotNull { it }.collect {
                        if (it)
                            showSendButton()
                        else
                            hideSendButton()
                    }
                }

                launch {
                    captionEnabledStateFlow.mapNotNull { it }.collect {
                        if (it)
                            showCaptionLayout(withAnim = true)
                        else
                            hideCaptionLayout(withAnim = true)
                    }
                }

                launch {
                    mediaCountStateFlow.collect {
                        setupMediaCountView(it)
                    }
                }
            }
        }

        galleryViewModel.currentFragmentLiveData.observe(this@GalleryActivity) { galleryView ->
            replaceFragment(galleryView)
        }
    }

    private fun handleGalleryResults(it: Array<String>?) {
        if (it != null && it.isNotEmpty()) {
            finishWithOKResult(it)
        } else {
            finishWithCancelResult()
        }
    }

    private fun showError(it: Int) {
        if (it == R.string.gallery_error_max_selectable) {
            Toast.makeText(this@GalleryActivity, getString(it, galleryOptions.maxSelectableMedia), Toast.LENGTH_SHORT).show()
        }
    }

    private fun replaceFragment(galleryView: GalleryView?) {
        when (galleryView) {
            is GalleryView.BucketList -> {
                if (!galleryViewModel.userSelectedMedias)
                    toolbarGalleryActivity.title = getString(galleryOptions.toolbarTitle)

                supportFragmentManager.beginTransaction()
                    .add(R.id.layoutFragmentContainer, BucketListFragment())
                    .commit()
                toolbarGalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible = true
            }
            is GalleryView.BucketContent -> {
                if (!galleryViewModel.userSelectedMedias)
                    toolbarGalleryActivity.title = galleryView.bucketName

                supportFragmentManager.beginTransaction()
                    .replace(R.id.layoutFragmentContainer, com.example.gallery.buckets.bucketContent.BaseBucketContentFragment()
                        .apply {
                        arguments = Bundle().apply {
                            putLong("bucket_id", galleryView.bucketId)
                        }
                    })
                    .addToBackStack(null)
                    .commit()
                toolbarGalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible = false
            }
            else -> Unit
        }
    }

    private fun setupMediaCountView(value: MediaCountModel) {
        if (value.selectedCount <= 0) {
            toolbarGalleryActivity.title =
                if (galleryViewModel.currentFragmentLiveData.value is GalleryView.BucketContent) galleryViewModel.currentFragmentLiveData.value!!.let { it as GalleryView.BucketContent }.bucketName else getString(
                    galleryOptions.toolbarTitle
                )
            toolbarGalleryActivity.setNavigationIcon(R.drawable.gallery_ic_back_arrow)
            toolbarGalleryActivity.setNavigationOnClickListener { onBackPressed() }
        } else {
            toolbarGalleryActivity.setNavigationIcon(R.drawable.gallery_ic_cancel)
            toolbarGalleryActivity.setNavigationOnClickListener { galleryViewModel.deselectAllSelections() }
            toolbarGalleryActivity.title = createMediaCountSpannable(
                context = this,
                value = value,
                colorAccent = GalleryActivityComponentHolder.getOrNull()?.provideGalleryStyleAttrs()?.galleryColorAccent ?: Color.BLUE
            )
        }
    }

    private fun initView() {
        addCameraMenuItem()
        addRecyclerViewItemViewModeMenuItem()
        toolbarGalleryActivity.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.bucketListMenuItemShowRecyclerViewItemModelChanger -> {

                    if (galleryOptions.bucketRecyclerViewItemMode == BucketRecyclerViewItemMode.LinearStyle) {
                        galleryOptions.bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.GridStyle
                    } else
                        galleryOptions.bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.LinearStyle

                    it.icon = getRecyclerViewItemViewModeIcon(galleryOptions.bucketRecyclerViewItemMode)
                    galleryViewModel.changeRecyclerViewItemMode(galleryOptions.bucketRecyclerViewItemMode)

                }
                R.id.bucketListMenuItemCamera -> takePhoto()
            }

            true
        }

        floatingButtonSendMedia.setOnClickListener {
            galleryViewModel.prepareSelectedResults()
        }
    }

    private fun addCameraMenuItem() {
        toolbarGalleryActivity.apply {
            galleryOptions.cameraEnabledOptions.also {
                if (it.enabled) {
                    if (it.fileProviderAuthority != null) {
                        menu.add(0, R.id.bucketListMenuItemCamera, 1, R.string.camera).apply {
                            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                            icon = AppCompatResources.getDrawable(
                                this@GalleryActivity,
                                R.drawable.gallery_icon_camera
                            )?.also(::setToolbarColorToMenuItemDrawable)
                        }
                    } else {
                        throw IllegalArgumentException("cant taking photo without fileProviderAuthority")
                    }
                }
            }
        }
    }


    private fun takePhoto() {
        galleryOptions.cameraEnabledOptions.also {
            val filename = generatePhotoFilename()
            (if (it.directory != null) {
                File(it.directory, filename)
            } else {
                File(GalleryActivityComponentHolder.getOrNull()!!.provideCacheDir().cacheDir, filename)
            }).also { temporaryFile ->
                getIntentForTakingPhoto(it.fileProviderAuthority!!, temporaryFile)?.also { takePhotoIntent ->
                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST_CODE)
                    galleryViewModel.setCameraPhotoFileAddress(temporaryFile.path)
                }
            }
        }
    }

    private fun getRecyclerViewItemViewModeIcon(mode: BucketRecyclerViewItemMode?): Drawable? {
        return AppCompatResources.getDrawable(
            this@GalleryActivity,
            if (mode?.value != R.layout.grid_bucket_item_view) R.drawable.gallery_grid_mode else R.drawable.gallery_linear_mode
        )?.also(::setToolbarColorToMenuItemDrawable)
    }

    // using BlendModeColorFilter for android Q and above. and setColorFilter for below of android Q
    @Suppress("DEPRECATION")
    private fun setToolbarColorToMenuItemDrawable(drawable: Drawable) {
        val tintColor = GalleryActivityComponentHolder.getOrNull()?.provideGalleryStyleAttrs()?.galleryToolbarIconTintColor ?: Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(
                tintColor,
                BlendMode.SRC_IN
            )
        } else
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
    }

    private fun addRecyclerViewItemViewModeMenuItem() {
        toolbarGalleryActivity.apply {
            galleryOptions.bucketItemModeToggleEnabled.also {
                val mode = galleryOptions.bucketRecyclerViewItemMode
                if (it) {
                    menu.add(0, R.id.bucketListMenuItemShowRecyclerViewItemModelChanger, 0, R.string.list_mode).apply {
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                        icon = getRecyclerViewItemViewModeIcon(mode)
                    }

                    showOrHideMenusBasedOnFragment()
                }
            }
        }
    }

    private fun showOrHideMenusBasedOnFragment() {
        try {
            toolbarGalleryActivity.menu?.findItem(R.id.bucketListMenuItemShowRecyclerViewItemModelChanger)?.isVisible =
                supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is BucketListFragment
        } catch (ignored: Throwable) {
            ignored.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults.first() == PackageManager.PERMISSION_GRANTED)
            initialize()
        else if (grantResults.isNotEmpty() && requestCode == WRITE_EXTERNAL_REQUEST_CODE && grantResults.first() == PackageManager.PERMISSION_DENIED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this@GalleryActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                showPermanentlyPermissionDeniedDialog()
            else
                writeExternalStoragePermissionDenied()
        }
    }

    private fun writeExternalStoragePermissionDenied() {
        AlertDialog.Builder(this@GalleryActivity, R.style.Gallery_AlertDialogTheme)
            .setMessage(R.string.access_external_storage_denied)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_button) { dialog, _ ->
                dialog.dismiss()
                initialize()
            }.setNegativeButton(R.string.exit_button) { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    private fun showPermanentlyPermissionDeniedDialog() {
        AlertDialog.Builder(this@GalleryActivity, R.style.Gallery_AlertDialogTheme)
            .setMessage(R.string.access_external_storage_permanently_denied)
            .setCancelable(false)
            .setPositiveButton(R.string.continue_button) { dialog, _ ->
                dialog.dismiss()
                try {
                    startActivity(getSettingIntent(this@GalleryActivity.applicationContext))
                } catch (activityNotFoundException: ActivityNotFoundException) {
                    activityNotFoundException.printStackTrace()
                    onBackPressed()
                }
            }.setNegativeButton(R.string.cancel_button) { dialog, _ ->
                dialog.dismiss()
                onBackPressed()
            }
            .show()
    }

    @Suppress("SameParameterValue")
    private fun hideCaptionLayout(withAnim: Boolean) {
        prepareCaptionViewStub()
        if (relativeLayoutCaptionLayout?.visibility == View.GONE) return
        frameLayoutCaptionHolder.findViewById<EditText>(R.id.galleryEditTextCaption)?.hideKeyboard()
        if (!withAnim)
            relativeLayoutCaptionLayout?.visibility = View.GONE
        else {
            relativeLayoutCaptionLayout?.startAnimation(
                TranslateAnimation(
                    0f, 0f, 0f, (relativeLayoutCaptionLayout?.height)?.toFloat() ?: 0f
                ).apply {
                    fillAfter = true
                    duration = 250
                    setOnAnimationEndListener {
                        relativeLayoutCaptionLayout.visibility = View.GONE
                        relativeLayoutCaptionLayout.animation = null
                    }
                }
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun showCaptionLayout(withAnim: Boolean) {
        prepareCaptionViewStub()
        if (relativeLayoutCaptionLayout?.visibility == View.VISIBLE) return
        if (!withAnim)
            relativeLayoutCaptionLayout?.visibility = View.VISIBLE
        else {
            relativeLayoutCaptionLayout.visibility = View.VISIBLE
            relativeLayoutCaptionLayout?.startAnimation(
                TranslateAnimation(
                    0f, 0f, ((relativeLayoutCaptionLayout?.height)?.toFloat() ?: 0f), 0f
                ).apply {
                    fillAfter = true
                    duration = 250
                    setOnAnimationEndListener {
                        relativeLayoutCaptionLayout.animation = null
                    }
                }
            )
        }
    }

    private fun prepareCaptionViewStub() {
        if (viewStubCaptionLayout != null && viewStubCaptionLayout.parent != null) {
            (try {
                viewStubCaptionLayout.inflate()
                imageViewSendMessage.setOnClickListener { galleryViewModel.prepareSelectedResults() }
                galleryOptions.captionEnabledOptions.editTextLayoutResId.let {
                    LayoutInflater.from(this).inflate(it, frameLayoutCaptionHolder, false).findViewById<AppCompatEditText>(R.id.galleryEditTextCaption).apply {
                        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
                    }
                }
            } catch (ignored: Throwable) {
                Log.e(GALLERY_LOG_TAG, "error while inflating captionLayoutResId. switch to default implementation")
                LayoutInflater.from(this).inflate(R.layout.caption_edit_text_layout, frameLayoutCaptionHolder, false)
                    .findViewById(R.id.galleryEditTextCaption)
            }).also {
                frameLayoutCaptionHolder.addView(it)
            }
        }

    }

    override fun onDestroy() {
        GalleryActivityComponentHolder.onDestroy()
        if (isFinishing) {
            GalleryCoreComponentHolder.onDestroy()
        }
        super.onDestroy()
    }

    private fun showSendButton(withAnim: Boolean = true) {
        if (frameLayoutSendMedia == null || frameLayoutSendMedia?.visibility == View.VISIBLE) return

        if (!withAnim) {
            frameLayoutSendMedia?.visibility = View.GONE
            return
        }

        floatingButtonSendMedia.visibility = View.INVISIBLE
        frameLayoutSendMedia.visibility = View.INVISIBLE
        frameLayoutSendMediaAnimationPostRunnable = Runnable {
            floatingButtonSendMedia.visibility = View.VISIBLE
            floatingButtonSendMedia.startAnimation(
                TranslateAnimation(
                    ((floatingButtonSendMedia?.height)?.toFloat() ?: 0f), 0f, 0f, 0f
                ).apply {
                    fillAfter = true
                    duration = 200
                    setOnAnimationEndListener {
                        floatingButtonSendMedia.animation = null
                    }
                }
            )

            frameLayoutSendMedia.visibility = View.VISIBLE
            frameLayoutSendMedia?.startAnimation(
                TranslateAnimation(
                    0f, 0f, ((frameLayoutSendMedia?.height)?.toFloat() ?: 0f), 0f
                ).apply {
                    fillAfter = true
                    duration = 200
                    setOnAnimationEndListener {
                        frameLayoutSendMedia.animation = null
                    }
                }
            )
        }

        frameLayoutSendMediaAnimationPostRunnable?.also(frameLayoutSendMedia::post)
    }

    private fun hideSendButton(withAnim: Boolean = true) {
        if (frameLayoutSendMediaAnimationPostRunnable != null) {
            frameLayoutSendMedia?.removeCallbacks(frameLayoutSendMediaAnimationPostRunnable)
            frameLayoutSendMediaAnimationPostRunnable = null
        }

        if (frameLayoutSendMedia?.visibility == View.GONE) return

        if (!withAnim) {
            frameLayoutSendMedia?.visibility = View.VISIBLE
            return
        }

        floatingButtonSendMedia.startAnimation(
            TranslateAnimation(
                0f, ((floatingButtonSendMedia?.height)?.toFloat() ?: 0f), 0f, 0f
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    floatingButtonSendMedia.visibility = View.GONE
                    floatingButtonSendMedia.animation = null
                }
            }
        )

        frameLayoutSendMedia?.startAnimation(
            TranslateAnimation(
                0f, 0f, 0f, ((frameLayoutSendMedia?.height)?.toFloat() ?: 0f)
            ).apply {
                fillAfter = true
                duration = 200
                setOnAnimationEndListener {
                    frameLayoutSendMedia.visibility = View.GONE
                    frameLayoutSendMedia.animation = null
                }
            }
        )
    }


    override fun showToolbar(withAnim: Boolean) {
        if (toolbarGalleryActivity.visibility == View.VISIBLE) return
        fun setHeightOfFragmentContainer() {
            layoutFragmentContainer.layoutParams = (layoutFragmentContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            }
        }
        if (!withAnim) {
            setHeightOfFragmentContainer()
            toolbarGalleryActivity?.visibility = View.VISIBLE
            return
        }

        toolbarGalleryActivity.startAnimation(TranslateAnimation(0f, 0f, -toolbarGalleryActivity.height.toFloat(), 0f).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                setHeightOfFragmentContainer()
                toolbarGalleryActivity.visibility = View.VISIBLE
                toolbarGalleryActivity.animation = null
            }
        })
    }

    override fun hideToolbar(withAnim: Boolean) {
        if (toolbarGalleryActivity.visibility == View.GONE) return
        fun setHeightOfFragmentContainer() {
            layoutFragmentContainer.layoutParams = (layoutFragmentContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                height = ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }
        if (!withAnim) {
            setHeightOfFragmentContainer()
            toolbarGalleryActivity?.visibility = View.GONE
            return
        }

        toolbarGalleryActivity.startAnimation(TranslateAnimation(0f, 0f, 0f, -toolbarGalleryActivity.height.toFloat()).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                setHeightOfFragmentContainer()
                toolbarGalleryActivity.visibility = View.GONE
                toolbarGalleryActivity.animation = null
            }
        })
    }

    override fun onBackPressed() {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible && fragment.childFragmentManager.backStackEntryCount > 1) {
                fragment.childFragmentManager.popBackStack()
                return
            }
        }

        if (supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is BucketListFragment && galleryViewModel.userSelectedMedias)
            galleryViewModel.deselectAllSelections()
        else if (supportFragmentManager.findFragmentById(R.id.layoutFragmentContainer) is com.example.gallery.buckets.bucketContent.BaseBucketContentFragment) {
            super.onBackPressed()
            if (!galleryViewModel.userSelectedMedias) {
                toolbarGalleryActivity.title = getString(galleryOptions.toolbarTitle)
            }
            galleryViewModel.clearLatestValueOfCurrentFragmentLiveData()
        } else {
            super.onBackPressed()
        }

        showOrHideMenusBasedOnFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handleTakingPhotoResult()
            } else {
                galleryViewModel.clearCameraPhotoFileAddress()
            }
        }
    }

    private fun handleTakingPhotoResult() {
        galleryViewModel.prepareCameraResultWithSelectedResults()
    }

    private fun finishWithCancelResult() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onStop() {
        frameLayoutSendMediaAnimationPostRunnable?.also {
            frameLayoutSendMedia?.removeCallbacks(it)
        }
        super.onStop()
    }

    private fun finishWithOKResult(it: Array<String>) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(GALLERY_MEDIAS_LIST_KEY, it)
            if (galleryOptions.captionEnabledOptions.enabled) {
                putExtra(GALLERY_CAPTION_KEY, frameLayoutCaptionHolder.findViewById<EditText>(R.id.galleryEditTextCaption)?.text?.toString()?.trim())
            }
        })
        finish()
    }
}