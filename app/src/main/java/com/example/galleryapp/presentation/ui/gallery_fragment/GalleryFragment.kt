package com.example.galleryapp.presentation.ui.gallery_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.gallery.main.gallery.GalleryOptions
import com.example.gallery.main.gallery.openGallery
import com.example.galleryapp.R
import com.example.galleryapp.databinding.FragmentGalleryBinding
import com.example.galleryapp.utils.GlideImageLoader

class GalleryFragment : Fragment() {

    private val galleryRequestCode = 830
    private val glideImageLoader by lazy { GlideImageLoader() }
    private lateinit var binding: FragmentGalleryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false)

        initView()

        return binding.root
    }

    private fun initView() {

        binding.cvAlbum.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        openGallery(galleryRequestCode, GalleryOptions(glideImageLoader))
    }
}