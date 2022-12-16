package com.example.gallery.repo

import com.example.gallery.models.BucketType
import com.example.gallery.models.MediaBucket

interface AbstractMediaBucketProvider {

    suspend fun getMediaBuckets(bucketType: BucketType): List<MediaBucket>

}