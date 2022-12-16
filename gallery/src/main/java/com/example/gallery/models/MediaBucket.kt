package com.example.gallery.models

import java.io.Serializable

data class MediaBucket(
    val id: Long,
    val bucketPath: String,
    val displayName: String,
    val firstMediaThumbPath: String,
    val mediaCount: Int = 1
) : Serializable