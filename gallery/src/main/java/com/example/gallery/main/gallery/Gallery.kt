@file:JvmName("Gallery")

package com.example.gallery.main.gallery

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.fragment.app.Fragment
import com.example.gallery.main.di.GalleryCoreComponentHolder
import com.example.gallery.main.ui.GalleryActivity
import com.example.gallery.utils.GALLERY_CAPTION_KEY
import com.example.gallery.utils.GALLERY_MEDIAS_LIST_KEY

@JvmName("startGalleryFromActivityWithOptions")
fun Activity.startGalleryWithOptions(requestCode: Int, galleryOptions: GalleryOptions) {
    GalleryCoreComponentHolder.createComponent(galleryOptions)
    startActivityForResult(Intent(this, GalleryActivity::class.java), requestCode)
}

@JvmName("startGalleryFromFragmentWithOptions")
fun Fragment.startGalleryWithOptions(requestCode: Int, galleryOptions: GalleryOptions) {
    GalleryCoreComponentHolder.createComponent(galleryOptions)
    startActivityForResult(Intent(this.requireContext(), GalleryActivity::class.java), requestCode)
}

@JvmName("openGallery")
fun Fragment.openGallery(requestCode: Int, galleryOptions: GalleryOptions) {
    GalleryCoreComponentHolder.createComponent(galleryOptions)
    startActivityForResult(Intent(this.requireContext(), GalleryActivity::class.java), requestCode)
}

@JvmName("getResultMediasFromIntent")
fun Intent.getGalleryResultMediasFromIntent(): Array<String>? {
    if (this.hasExtra(GALLERY_MEDIAS_LIST_KEY)) {
        return this.getStringArrayExtra(GALLERY_MEDIAS_LIST_KEY)
    } else {
        throw IllegalArgumentException("input intent has no extra with key $GALLERY_MEDIAS_LIST_KEY")
    }
}

@JvmName("getCaptionFromIntent")
fun Intent.getGalleryCaptionFromIntent(): String? = this.getStringExtra(GALLERY_CAPTION_KEY)