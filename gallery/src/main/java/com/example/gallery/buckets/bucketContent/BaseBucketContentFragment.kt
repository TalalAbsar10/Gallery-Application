package com.example.gallery.buckets.bucketContent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gallery.R
import com.example.gallery.buckets.bucketContent.content.BucketContentFragment
import com.example.gallery.buckets.bucketContent.preview.PreviewFragment
import com.example.gallery.main.di.GalleryActivityComponentHolder

internal class BaseBucketContentFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_base_bucket_content, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        if (savedInstanceState == null) {
            arguments?.getLong("bucket_id")?.also { bucketId ->
                childFragmentManager.beginTransaction()
                    .replace(R.id.layoutBucketContentFragmentContainer, BucketContentFragment().apply {
                        arguments = Bundle().apply {
                            putLong("bucket_id", bucketId)
                        }
                    })
                    .addToBackStack(null)
                    .commit()
            } ?: requireActivity().onBackPressed()
        }
    }

    private fun initViewModel() {
        ViewModelProvider(
            this,
            GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentViewModelFactory()
        )[com.example.gallery.buckets.bucketContent.BucketContentViewModel::class.java].apply {
            showPreviewFragmentLiveData.observe(viewLifecycleOwner) {
                if (it != null) {
                    navigateToPreviewFragment(fromMediaPath = it)
                }
            }
        }
    }

    private fun navigateToPreviewFragment(fromMediaPath: String) {
        childFragmentManager.beginTransaction()
            .replace(R.id.layoutBucketContentFragmentContainer, PreviewFragment().apply {
                arguments = Bundle().apply {
                    putString("from_media_path", fromMediaPath)
                }
            })
            .addToBackStack(null)
            .commit()
    }

}