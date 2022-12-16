package com.example.gallery.main.di.component

import com.example.gallery.main.di.module.GalleryCoreModule
import com.example.gallery.main.gallery.GalleryOptions

internal class GalleryCoreComponentBuilder {

    private var galleryOptions: GalleryOptions? = null

    fun bindGalleryOptions(galleryOptions: GalleryOptions): GalleryCoreComponentBuilder {
        this.galleryOptions = galleryOptions
        return this
    }

    fun build(): GalleryCoreModule {
        require(galleryOptions != null) { "galleryOptions must not be null" }

        return GalleryCoreModule(galleryOptions!!)
    }
}