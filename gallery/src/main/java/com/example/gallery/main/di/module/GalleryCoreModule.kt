package com.example.gallery.main.di.module

import com.example.gallery.imageLoader.GalleryImageLoader
import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.main.di.component.GalleryCoreComponent
import com.example.gallery.repo.AbstractBucketContentProvider
import com.example.gallery.repo.AbstractMediaBucketProvider
import com.example.gallery.repo.MediaBucketProvider
import java.lang.NullPointerException

internal class GalleryCoreModule constructor(
    private val galleryOptions: GalleryOptions
) : GalleryCoreComponent {

    private var defaultImageLoader: GalleryImageLoader? = null
    private var abstractMediaBucketProvider: AbstractMediaBucketProvider? = null
    private var abstractBucketContentProvider: AbstractBucketContentProvider? = null

    override fun provideGalleryOptions(): GalleryOptions = galleryOptions

    override fun provideImageLoader(): GalleryImageLoader = galleryOptions.imageLoader ?: throw NullPointerException("imageLoader must not be null")

    override fun provideBucketProvider(): AbstractMediaBucketProvider =
        galleryOptions.bucketProviderAbstract ?: throw IllegalArgumentException("${MediaBucketProvider::class.simpleName} can provide only in feature component holders")

    override fun provideBucketContentProvider(): AbstractBucketContentProvider = galleryOptions.abstractBucketContentProvider
        ?: throw IllegalArgumentException("${AbstractBucketContentProvider::class.simpleName} can provide only in feature component holders")

    override fun releaseCoreComponent() {
        defaultImageLoader = null
        abstractMediaBucketProvider = null
        abstractBucketContentProvider = null
    }

}