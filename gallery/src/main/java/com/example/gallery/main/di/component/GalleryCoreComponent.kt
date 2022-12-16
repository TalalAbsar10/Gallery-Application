package com.example.gallery.main.di.component

import com.example.gallery.imageLoader.GalleryImageLoader
import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.repo.AbstractBucketContentProvider
import com.example.gallery.repo.AbstractMediaBucketProvider

internal interface GalleryCoreComponent {

    fun provideGalleryOptions(): GalleryOptions

    fun provideImageLoader(): GalleryImageLoader

    fun provideBucketProvider(): AbstractMediaBucketProvider

    fun provideBucketContentProvider(): AbstractBucketContentProvider

    fun releaseCoreComponent()
}