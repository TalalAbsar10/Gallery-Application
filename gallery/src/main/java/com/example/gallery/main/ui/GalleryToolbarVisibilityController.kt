package com.example.gallery.main.ui

internal interface GalleryToolbarVisibilityController {
    fun showToolbar(withAnim: Boolean = false)
    fun hideToolbar(withAnim: Boolean = false)
}