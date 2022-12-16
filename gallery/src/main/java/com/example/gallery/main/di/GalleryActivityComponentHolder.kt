package com.example.gallery.main.di

import androidx.fragment.app.FragmentActivity
import com.example.gallery.main.di.component.GalleryActivityComponent
import com.example.gallery.main.di.component.GalleryActivityComponentBuilder
import com.example.gallery.utils.AbstractFeatureComponentHolder

internal object GalleryActivityComponentHolder :
    AbstractFeatureComponentHolder<GalleryActivityComponent>() {

    override fun componentCreator(activity: FragmentActivity): GalleryActivityComponent {
        return GalleryActivityComponentBuilder().plusGalleryActivity(galleryFragmentActivity = activity)
            .plusGalleryCoreComponent(GalleryCoreComponentHolder.getOrThrow())
            .build()
    }

    override fun onDestroy() {
        this.getOrNull()?.releaseBucketListComponent()
        super.onDestroy()
    }
}