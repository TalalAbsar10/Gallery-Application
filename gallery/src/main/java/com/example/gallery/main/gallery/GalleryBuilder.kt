package com.example.gallery.main.gallery

import android.content.pm.ActivityInfo
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.R
import com.example.gallery.imageLoader.GalleryImageLoader
import com.example.gallery.models.BucketType
import com.example.gallery.repo.AbstractBucketContentProvider
import com.example.gallery.repo.AbstractMediaBucketProvider

/**
 * Builder for [GalleryOptions]
 */
class GalleryBuilder constructor(private var galleryOptions: GalleryOptions = GalleryOptions(null)) {

    /**
     * filter buckets that contains [bucketType]
     */
    fun mediaTypeFiltering(
        bucketType: BucketType
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            mediaTypeFilter = bucketType
        )
        return this
    }

    /**
     * set [maxSelectableMedia] as max media count selected by user. default value is [UNLIMITED_SELECT]
     */
    fun setMaxSelectableMedia(
        maxSelectableMedia: Int
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            maxSelectableMedia = maxSelectableMedia
        )
        return this
    }


    /**
     * enable or disable taking photo from camera in gallery.
     * if you enable camera you must set [CameraEnabledOptions.fileProviderAuthority]
     */
    fun setCameraEnabledOptions(
        cameraEnabledOptions: CameraEnabledOptions
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            cameraEnabledOptions = cameraEnabledOptions
        )
        return this
    }

    /**
     * enable or disable sending caption with medias. and customize editText and send icon.
     *
     * note: if you are using customEditText(like emojiEdiText) in your app you can pass
     * editText as layout res to [CaptionEnabledOptions.editTextLayoutResId]. its important
     * your customEditText is root of layout file and id must be equal to [R.id.galleryEditTextCaption]
     * @return GalleryBuilder
     */
    fun setCaptionEnabledOptions(
        captionEnabledOptions: CaptionEnabledOptions
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            captionEnabledOptions = captionEnabledOptions
        )
        return this
    }

    /**
     * enable or disable showing media count
     */
    fun setMediaCountEnabled(
        enable: Boolean
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(mediaCountEnabled = enable)
        return this
    }

    /**
     * set current theme of gallery. gallery support dracula [R.style.Gallery_Dracula]
     * and light [R.style.Gallery_Light] theme internally. but you can create and set your own theme.
     * @param theme Int style res of gallery theme
     */
    fun setTheme(
        @StyleRes theme: Int
    ): GalleryBuilder {

        galleryOptions = galleryOptions.copy(themeResId = theme)
        return this
    }

    /**
     * set orientation of GalleryActivity. default value is [ActivityInfo.SCREEN_ORIENTATION_USER]
     * @param orientationMode Int require ActivityInfo.ScreenOrientation constants
     */
    fun setOrientation(
        orientationMode: Int
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            orientationMode = orientationMode
        )
        return this
    }

    /**
     * gallery does not use image loading libraries like glide, picasso internally.
     * you must implement [GalleryImageLoader] and load requested photo or gif into their image view.
     * with glide, picasso or custom image loader
     * @param imageLoader GalleryImageLoader implement of [GalleryImageLoader]
     */
    fun setImageLoader(
        imageLoader: GalleryImageLoader
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            imageLoader = imageLoader
        )

        return this
    }

    /**
     * set your custom bucket provider by implement [AbstractMediaBucketProvider] and
     * set your custom bucket content provider by implement [AbstractBucketContentProvider]
     * @param bucketContentProvider AbstractBucketContentProvider custom implementation of [AbstractBucketContentProvider]
     */
    fun setContentProviders(
        bucketContentProvider: AbstractBucketContentProvider,
        bucketProvider: AbstractMediaBucketProvider
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            abstractBucketContentProvider = bucketContentProvider,
            bucketProviderAbstract = bucketProvider
        )

        return this
    }

    /**
     * set default [bucketRecyclerViewItemMode]. you can enable or disable
     * changing bucket item mode by user with [setBucketItemModeToggleEnabled]
     */
    fun setBucketItemMode(
        bucketRecyclerViewItemMode: BucketRecyclerViewItemMode
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            bucketRecyclerViewItemMode = bucketRecyclerViewItemMode
        )
        return this
    }

    /**
     * enable or disable changing bucket item mode by user
     */
    fun setBucketItemModeToggleEnabled(
        enable: Boolean
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            bucketItemModeToggleEnabled = enable
        )
        return this
    }

    /**
     * if new media add or remove in the device, gallery reload the buckets or bucket content. by defaults
     * media observer is disabled if you want to enable it just pass true to [setMediaObserverEnabled]
     */
    fun setMediaObserverEnabled(enable: Boolean): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            mediaObserverEnabled = enable
        )

        return this
    }

    /**
     * set toolbar title text of galleryActivity
     */
    fun setGalleryToolbarTitleText(@StringRes titleRes: Int): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            toolbarTitle = titleRes
        )

        
        return this
    }

    /**
     * set orientation for photo preview viewPager
     */
    fun setMediaPreviewViewPagerOrientation(@ViewPager2.Orientation orientation: Int): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            mediaPreviewScrollOrientation = orientation
        )

        return this
    }

    /**
     * set pagerTransformer for photo preview viewPager
     */
    fun setMediaPreviewPageTransformer(pageTransformer: ViewPager2.PageTransformer?): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            mediaPreviewPageTransformer = pageTransformer
        )

        return this
    }

    /**
     * set toggle background color when media selected
     */
    fun setSelectedMediaToggleBackgroundColor(
        color: Int
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            selectedMediaToggleBackgroundColor = color
        )

        return this
    }

    /**
     * set onClick listener for video play toggle
     */
    fun setOnVideoPlayClick(
        onClick: (path: String) -> Unit
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            onVideoPlayClick = onClick
        )

        return this
    }

    /**
     * if true gallery request external storage permission from user
     */
    fun setGrantExternalStoragePermission(
        grantExternalStoragePermission: Boolean
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            grantExternalStoragePermission = grantExternalStoragePermission
        )

        return this
    }

    /**
     * if true gallery request shared storage permission in android +10
     * default value for android +10 is true
     */
    fun setGrantSharedStoragePermission(
        grantSharedStorePermission: Boolean
    ): GalleryBuilder {
        galleryOptions = galleryOptions.copy(
            grantSharedStorePermission = grantSharedStorePermission
        )

        return this
    }

    /**
     * Set [GalleryBucketsSpanCountMode] for bucket content recyclerView.
     *
     * If @param newMode == [GalleryBucketsSpanCountMode.Automatic] the span count
     * will be calculate automatically based on device width.
     *
     * If @param newMode == [GalleryBucketsSpanCountMode.UserZoomInOrZoomOut] ths span count
     * will be calculate automatically based on device with but user can change it
     * by zoom-in and zoom-out on bucket-content recycler-view.
     */
    fun setGallerySpanCountMode(newMode: GalleryBucketsSpanCountMode) : GalleryBuilder {
        galleryOptions = galleryOptions.copy(galleryBucketsSpanCountMode = newMode)
        return this
    }

    fun build(): GalleryOptions {
        require(galleryOptions.imageLoader != null) { "You must set imageLoader" }

        if (galleryOptions.cameraEnabledOptions.enabled)
            require(galleryOptions.cameraEnabledOptions.fileProviderAuthority != null) { "fileProviderAuthority must not be null" }

        if (galleryOptions.grantSharedStorePermission) {
            if (!galleryOptions.grantExternalStoragePermission) {
                throw IllegalArgumentException("grantExternalStoragePermission must be true if you want to gallery grant shared storage permission on android +10")
            }
        }

        return galleryOptions
    }
}