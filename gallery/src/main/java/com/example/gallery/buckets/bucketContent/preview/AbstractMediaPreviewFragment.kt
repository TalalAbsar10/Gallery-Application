package com.example.gallery.buckets.bucketContent.preview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.gallery.models.Media

internal abstract class AbstractMediaPreviewFragment constructor(layoutId: Int) : Fragment(layoutId) {

    var onMediaPreviewClickListener: View.OnClickListener? = null

    companion object {
        @JvmName("fromVideo")
        fun from(video: Media.Video): AbstractMediaPreviewFragment = VideoPreviewFragment()
            .apply {
                arguments = Bundle().apply {
                    putParcelable("video", video)
                }
            }

        @JvmName("fromPhoto")
        fun from(photo: Media.Photo): AbstractMediaPreviewFragment = PhotoPreviewFragment()
            .apply {
                arguments = Bundle().apply {
                    putParcelable("photo", photo)
                }
            }
    }
}