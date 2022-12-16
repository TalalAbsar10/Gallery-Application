package com.example.gallery.main.ui

internal sealed class GalleryView {
    object BucketList : GalleryView()
    data class BucketContent(val bucketId: Long, val bucketName: String) : GalleryView()
}