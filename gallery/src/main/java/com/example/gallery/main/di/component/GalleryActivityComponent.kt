package com.example.gallery.main.di.component

import android.content.ContentResolver
import android.graphics.drawable.Drawable
import androidx.fragment.app.FragmentActivity
import com.example.gallery.buckets.bucketContent.content.adapter.BucketContentAdapter
import com.example.gallery.buckets.bucketList.adapter.BucketListAdapter
import com.example.gallery.buckets.bucketList.adapter.MediaBucketDiffCallback
import com.example.gallery.models.CacheDir
import com.example.gallery.models.GalleryStyleAttrs
import com.example.gallery.utils.BucketContentViewModelFactory
import com.example.gallery.utils.BucketListViewModelFactory
import com.example.gallery.utils.GalleryViewModelFactory
import com.example.gallery.utils.MediaStoreObserver

internal interface GalleryActivityComponent : GalleryCoreComponent {

    fun provideBucketListViewModelFactory(): BucketListViewModelFactory

    fun provideActivity(): FragmentActivity

    fun releaseBucketListComponent()

    fun provideMediaBucketDiffCallback(): MediaBucketDiffCallback

    fun provideBucketContentAdapter(): BucketContentAdapter

    fun provideBucketContentViewModelFactory(): BucketContentViewModelFactory

    fun provideGalleryStyleAttrs(): GalleryStyleAttrs

    fun provideCacheDir(): CacheDir

    fun provideContentResolver(): ContentResolver

    fun provideGalleryViewModelFactory(): GalleryViewModelFactory

    fun provideSelectedDrawable(): Drawable

    fun provideDeselectedDrawable(): Drawable

    fun provideBucketListAdapter(): BucketListAdapter

    fun provideMediaStoreObserver(): MediaStoreObserver
}