package com.example.gallery.buckets.bucketContent.preview.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gallery.buckets.bucketContent.preview.AbstractMediaPreviewFragment
import com.example.gallery.buckets.bucketContent.preview.PreviewFragment
import com.example.gallery.models.Media

internal class MediaPreviewAdapter constructor(
    previewFragment: PreviewFragment,
    private val onViewPagerClick: View.OnClickListener? = null
) : FragmentStateAdapter(previewFragment) {

    var medias: List<Media>? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = medias?.size ?: 0

    override fun createFragment(position: Int): Fragment = (medias?.getOrNull(position)?.let {
        when (it) {
            is Media.Photo -> AbstractMediaPreviewFragment.from(it)
            is Media.Video -> AbstractMediaPreviewFragment.from(it)
        }
    } ?: AbstractMediaPreviewFragment.from(Media.Photo.empty())).apply {
        onMediaPreviewClickListener = onViewPagerClick
    }
}