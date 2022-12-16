package com.example.gallery.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gallery.buckets.bucketContent.BucketContentViewModel
import com.example.gallery.buckets.bucketList.BucketListViewModel
import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.main.ui.GalleryViewModel
import com.example.gallery.models.BucketType
import com.example.gallery.repo.AbstractBucketContentProvider
import com.example.gallery.repo.AbstractMediaBucketProvider

internal class BucketListViewModelFactory(
    private val abstractMediaBucketProvider: AbstractMediaBucketProvider,
    private val bucketType: BucketType,
    private val mediaObserverEnabled: Boolean,
    private val mediaStoreObserver: MediaStoreObserver
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(BucketListViewModel::class.java)) {
            BucketListViewModel(
                mediaObserverEnabled = mediaObserverEnabled,
                abstractMediaBucketProvider = abstractMediaBucketProvider,
                bucketType = bucketType,
                mediaStoreObserver = mediaStoreObserver
            ) as T
        } else throw IllegalArgumentException("this factory is just for BucketListViewModel")
}

internal class GalleryViewModelFactory(
    private val galleryOptions: GalleryOptions,
    private val mediaStoreObserver: MediaStoreObserver
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
        GalleryViewModel(galleryOptions, mediaStoreObserver) as T
    } else throw IllegalArgumentException("this factory is just for GalleryViewModel")

}


class BucketContentViewModelFactory constructor(
    private val abstractBucketContentProvider: AbstractBucketContentProvider,
    private val bucketType: BucketType
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = if (modelClass.isAssignableFrom(BucketContentViewModel::class.java)) {
        BucketContentViewModel(
            abstractBucketContentProvider, bucketType
        ) as T
    } else throw IllegalArgumentException("this factory is just for BucketContentViewModel")
}