package com.example.gallery.main.di.component

import androidx.fragment.app.FragmentActivity
import com.example.gallery.main.di.module.GalleryActivityModule


internal class GalleryActivityComponentBuilder {

    private var galleryFragmentActivity: FragmentActivity? = null
    private var galleryCoreComponent: GalleryCoreComponent? = null

    fun plusGalleryActivity(galleryFragmentActivity: FragmentActivity): GalleryActivityComponentBuilder =
        this.apply {
            this.galleryFragmentActivity = galleryFragmentActivity
        }

    fun plusGalleryCoreComponent(galleryCoreComponent: GalleryCoreComponent) : GalleryActivityComponentBuilder = this.apply {
        this.galleryCoreComponent = galleryCoreComponent
    }

    fun build(): GalleryActivityModule {
        require(galleryFragmentActivity != null) { "galleryActivity must set " }
        require(galleryCoreComponent != null) { "galleryCoreComponent must set " }
        return GalleryActivityModule(
            galleryFragmentActivity!!.applicationContext, galleryFragmentActivity!!, galleryCoreComponent!!
        )
    }
}