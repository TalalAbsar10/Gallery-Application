package com.example.gallery.main.di

import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.main.di.component.GalleryCoreComponent
import com.example.gallery.main.di.component.GalleryCoreComponentBuilder

internal object GalleryCoreComponentHolder {

    private var galleryCoreComponent: GalleryCoreComponent? = null

    fun createComponent(galleryOptions: GalleryOptions) {
        if (galleryCoreComponent == null)
            galleryCoreComponent = GalleryCoreComponentBuilder()
                .bindGalleryOptions(galleryOptions).build()
    }

    fun getOrThrow(): GalleryCoreComponent {
        require(galleryCoreComponent != null) {
            "galleryCoreComponent can't be null. please just use gallery.startGalleryInActivity or Gallery.startGalleryInFragment for starting gallery"
        }
        return galleryCoreComponent!!
    }

    /**
     * this method must be called when gallery views(all) destroyed
     */
    fun onDestroy() {
        galleryCoreComponent?.releaseCoreComponent()
        galleryCoreComponent = null
    }
}