package com.example.gallery.main.gallery

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.R
import com.example.gallery.imageLoader.GalleryImageLoader
import com.example.gallery.models.BucketType
import com.example.gallery.repo.AbstractBucketContentProvider
import com.example.gallery.repo.AbstractMediaBucketProvider
import com.example.gallery.utils.EnumType

data class GalleryOptions(
    val mediaTypeFilter: BucketType = BucketType.VIDEO_PHOTO_BUCKETS,
    val maxSelectableMedia: Int = UNLIMITED_SELECT,
    val cameraEnabledOptions: CameraEnabledOptions,
    val captionEnabledOptions: CaptionEnabledOptions,
    val mediaCountEnabled: Boolean,
    val imageLoader: GalleryImageLoader?,
    val bucketProviderAbstract: AbstractMediaBucketProvider?,
    val abstractBucketContentProvider: AbstractBucketContentProvider?,
    @StyleRes val themeResId: Int,
    val orientationMode: Int,
    var bucketRecyclerViewItemMode: BucketRecyclerViewItemMode,
    val bucketItemModeToggleEnabled: Boolean,
    var mediaObserverEnabled: Boolean,
    @StringRes var toolbarTitle: Int = R.string.gallery_toolbar_title,
    val mediaPreviewPageTransformer: ViewPager2.PageTransformer? = null,
    val mediaPreviewScrollOrientation: Int = ViewPager2.ORIENTATION_HORIZONTAL,
    val selectedMediaToggleBackgroundColor: Int = Color.parseColor("#A11183"),
    val onVideoPlayClick: ((path: String) -> Unit)? = null,
    val grantExternalStoragePermission: Boolean = true,
    val grantSharedStorePermission: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q, // required for android +10,
    val galleryBucketsSpanCountMode: GalleryBucketsSpanCountMode = GalleryBucketsSpanCountMode.Automatic
) {
    constructor(galleryImageLoader: GalleryImageLoader?) : this(
        mediaTypeFilter = BucketType.VIDEO_PHOTO_BUCKETS,
        maxSelectableMedia = UNLIMITED_SELECT,
        cameraEnabledOptions = CameraEnabledOptions(),
        captionEnabledOptions = CaptionEnabledOptions(),
        mediaCountEnabled = true,
        imageLoader = galleryImageLoader,
        bucketProviderAbstract = null,
        abstractBucketContentProvider = null,
        themeResId = R.style.Gallery_Light,
        orientationMode = ActivityInfo.SCREEN_ORIENTATION_USER,
        bucketRecyclerViewItemMode = BucketRecyclerViewItemMode.GridStyle,
        bucketItemModeToggleEnabled = true,
        mediaObserverEnabled = false,
        toolbarTitle = R.string.gallery_toolbar_title,
        mediaPreviewPageTransformer = null,
        mediaPreviewScrollOrientation = ViewPager2.ORIENTATION_HORIZONTAL,
        selectedMediaToggleBackgroundColor = Color.parseColor("#A11183"),
        onVideoPlayClick = null,
        grantExternalStoragePermission = true,
        grantSharedStorePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
        galleryBucketsSpanCountMode = GalleryBucketsSpanCountMode.Automatic
    )
}

const val UNLIMITED_SELECT = 0

data class CameraEnabledOptions(
    val enabled: Boolean = false,
    val fileProviderAuthority: String? = null,
    val directory: String? = null
) {
    constructor(enabled: Boolean, fileProviderAuthority: String) : this(
        enabled,
        fileProviderAuthority,
        null
    )
}

data class CaptionEnabledOptions(
    val enabled: Boolean = false,
    @DrawableRes val sendIcon: Int = R.drawable.gallery_icon_send,
    @LayoutRes val editTextLayoutResId: Int = R.layout.caption_edit_text_layout
) {
    constructor(enabled: Boolean) : this(
        enabled,
        R.drawable.gallery_icon_send,
        R.layout.caption_edit_text_layout
    )
}


enum class BucketRecyclerViewItemMode constructor(override var value: Int) : EnumType<Int> {
    GridStyle(R.layout.grid_bucket_item_view),
    LinearStyle(R.layout.linear_bucket_item_view)
}

enum class GalleryBucketsSpanCountMode {
    Automatic, // based on device width
    UserZoomInOrZoomOut // based on device width but users can changed span count by zoomIn or ZoomOut
}