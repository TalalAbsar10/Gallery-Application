package com.example.gallery.main.di.module

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import com.example.gallery.buckets.bucketContent.content.adapter.BucketContentAdapter
import com.example.gallery.buckets.bucketList.adapter.BucketListAdapter
import com.example.gallery.buckets.bucketList.adapter.MediaBucketDiffCallback
import com.example.gallery.imageLoader.GalleryImageLoader
import com.example.gallery.main.di.component.GalleryActivityComponent
import com.example.gallery.main.di.component.GalleryCoreComponent
import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.models.CacheDir
import com.example.gallery.models.GalleryStyleAttrs
import com.example.gallery.models.getGalleryStyleAttrs
import com.example.gallery.repo.AbstractBucketContentProvider
import com.example.gallery.repo.AbstractMediaBucketProvider
import com.example.gallery.repo.BucketContentProvider
import com.example.gallery.repo.MediaBucketProvider
import com.example.gallery.utils.*
import java.lang.ref.WeakReference

internal class GalleryActivityModule(
    private val context: Context,
    private val galleryFragmentActivity: FragmentActivity,
    private val galleryCoreComponent: GalleryCoreComponent
) : GalleryActivityComponent {

    private var abstractMediaBucketProvider: AbstractMediaBucketProvider? = null
    private var abstractBucketContentProvider: AbstractBucketContentProvider? = null
    private var bucketListViewModelFactory: BucketListViewModelFactory? = null
    private var galleryStyleAttrs: GalleryStyleAttrs? = null
    private var galleryMediaObserver: MediaStoreObserver? = null

    override fun provideBucketListViewModelFactory(): BucketListViewModelFactory =
        synchronized(bucketListViewModelFactory ?: this) {
            if (bucketListViewModelFactory == null) {
                bucketListViewModelFactory =
                    BucketListViewModelFactory(
                        abstractMediaBucketProvider = provideBucketProvider(),
                        bucketType = provideGalleryOptions().mediaTypeFilter,
                        mediaObserverEnabled = provideGalleryOptions().mediaObserverEnabled,
                        mediaStoreObserver = provideMediaStoreObserver()
                    )

                bucketListViewModelFactory!!
            } else {
                bucketListViewModelFactory!!
            }
        }

    override fun provideActivity(): FragmentActivity = galleryFragmentActivity

    override fun releaseBucketListComponent() {
        abstractMediaBucketProvider = null
        bucketListViewModelFactory = null
    }

    override fun provideMediaBucketDiffCallback(): MediaBucketDiffCallback =
        MediaBucketDiffCallback()

    override fun provideGalleryOptions(): GalleryOptions = galleryCoreComponent.provideGalleryOptions()
    override fun provideImageLoader(): GalleryImageLoader = galleryCoreComponent.provideImageLoader()
    override fun provideBucketProvider(): AbstractMediaBucketProvider = try {
        galleryCoreComponent.provideBucketProvider()
    } catch (ignored: Throwable) {
        provideGalleryBucketProvider()
    }

    override fun provideBucketContentProvider(): AbstractBucketContentProvider = try {
        galleryCoreComponent.provideBucketContentProvider()
    } catch (ignored: Throwable) {
        provideGalleryBucketContentProvider()
    }


    override fun releaseCoreComponent() = galleryCoreComponent.releaseCoreComponent()

    private fun provideGalleryBucketContentProvider(): AbstractBucketContentProvider =
        synchronized(abstractBucketContentProvider ?: this) {
            if (abstractBucketContentProvider == null) {
                abstractBucketContentProvider = BucketContentProvider(provideContentResolver(), provideCacheDir())
                abstractBucketContentProvider!!
            } else abstractBucketContentProvider!!
        }


    private fun provideGalleryBucketProvider(): AbstractMediaBucketProvider =
        synchronized(abstractMediaBucketProvider ?: this) {
            if (abstractMediaBucketProvider == null) {
                abstractMediaBucketProvider = MediaBucketProvider(provideCacheDir(), provideContentResolver())
                abstractMediaBucketProvider!!
            } else abstractMediaBucketProvider!!
        }

    override fun provideGalleryStyleAttrs(): GalleryStyleAttrs = synchronized(galleryStyleAttrs ?: this) {
        if (galleryStyleAttrs == null) {
            galleryStyleAttrs = galleryFragmentActivity.getGalleryStyleAttrs()
            galleryStyleAttrs!!
        } else
            galleryStyleAttrs!!
    }

    override fun provideBucketContentViewModelFactory(): BucketContentViewModelFactory = BucketContentViewModelFactory(
        provideBucketContentProvider(), provideGalleryOptions().mediaTypeFilter
    )

    override fun provideCacheDir(): CacheDir = CacheDir(context.externalCacheDir?.path ?: context.cacheDir.path)

    override fun provideContentResolver(): ContentResolver = context.contentResolver


    override fun provideBucketContentAdapter(): BucketContentAdapter =
        BucketContentAdapter(
            provideImageLoader(),
            provideSelectedDrawable(),
            provideDeselectedDrawable(),
            provideGalleryStyleAttrs().galleryPlaceHolderColor
        )

    override fun provideGalleryViewModelFactory(): GalleryViewModelFactory = GalleryViewModelFactory(
        provideGalleryOptions(), provideMediaStoreObserver()
    )

    override fun provideSelectedDrawable(): Drawable = createCircleDrawableWithStroke(
        provideGalleryOptions().selectedMediaToggleBackgroundColor, dpToPx(2), Color.WHITE
    )

    override fun provideDeselectedDrawable(): Drawable = createCircleDrawableWithStroke(
        Color.parseColor("#54000000"), dpToPx(2), Color.WHITE
    )

    override fun provideBucketListAdapter(): BucketListAdapter = BucketListAdapter(
        mediaBucketDiffCallback = provideMediaBucketDiffCallback(),
        imageLoader = provideImageLoader(),
        placeHolderColor = provideGalleryStyleAttrs().galleryPlaceHolderColor
    )

    override fun provideMediaStoreObserver(): MediaStoreObserver = synchronized(galleryMediaObserver ?: this) {
        if (galleryMediaObserver == null) {
            galleryMediaObserver = MediaStoreObserver(provideGalleryOptions().mediaObserverEnabled, Handler(Looper.getMainLooper()), WeakReference(provideActivity()))
        }

        galleryMediaObserver!!
    }
}