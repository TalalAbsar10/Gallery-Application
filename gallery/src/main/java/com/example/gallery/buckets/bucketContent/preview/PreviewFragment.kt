package com.example.gallery.buckets.bucketContent.preview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.gallery.R
import com.example.gallery.buckets.bucketContent.BucketContentViewModel
import com.example.gallery.buckets.bucketContent.preview.adapter.MediaPreviewAdapter
import com.example.gallery.main.di.GalleryActivityComponentHolder
import com.example.gallery.main.ui.GalleryToolbarVisibilityController
import com.example.gallery.main.ui.GalleryViewModel
import com.example.gallery.main.ui.MediaCountModel
import com.example.gallery.utils.createMediaCountSpannable
import com.example.gallery.utils.setOnAnimationEndListener
import kotlinx.android.synthetic.main.fragment_preview.*
import kotlinx.coroutines.launch

internal class PreviewFragment : Fragment(R.layout.fragment_preview), View.OnClickListener {

    private lateinit var bucketContentViewModel: BucketContentViewModel

    private lateinit var galleryViewModel: GalleryViewModel

    private val mediaPreviewAdapter by lazy {
        MediaPreviewAdapter(
            this@PreviewFragment,
            this
        )
    }
    private val selectedDrawable by lazy {
        GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideSelectedDrawable()
    }

    private val deselectDrawable by lazy {
        GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideDeselectedDrawable()
    }

    private val pageSelectedCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                showToolbarWithAnimation()
                checkForSelection(position)
                saveCurrentPosition(position)
            }
        }
    }

    private val galleryToolbarVisibilityController: GalleryToolbarVisibilityController by lazy {
        requireActivity() as GalleryToolbarVisibilityController
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun saveCurrentPosition(position: Int) {
        arguments?.putString(
            "from_media_path",
            bucketContentViewModel.getMediaPathByPosition(position)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        galleryToolbarVisibilityController.hideToolbar(false)
        initViewModel()
        initView()
    }

    override fun onStart() {
        super.onStart()
        viewPagerMediaPreview.registerOnPageChangeCallback(pageSelectedCallback)
        if (viewPagerMediaPreview.adapter == null)
            viewPagerMediaPreview.adapter = mediaPreviewAdapter
    }

    private fun initView() {
        GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideGalleryOptions().apply {
            viewPagerMediaPreview.orientation = mediaPreviewScrollOrientation
            viewPagerMediaPreview.setPageTransformer(mediaPreviewPageTransformer)
        }

        appCompatImageButtonMediaSelect.setOnClickListener {
            val position = viewPagerMediaPreview.currentItem
            bucketContentViewModel.getMediaPathByPosition(position)?.also {
                if (galleryViewModel.isPhotoSelected(it))
                    galleryViewModel.requestDeselectingMedia(it)
                else
                    galleryViewModel.requestSelectingMedia(it)
            }

            checkForSelection(position)
        }

        imageViewBackButton.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun checkForSelection(position: Int) {
        appCompatImageButtonMediaSelect.setImageDrawable(bucketContentViewModel.getMediaPathByPosition(position).let {
            if (it != null && galleryViewModel.isPhotoSelected(it)) selectedDrawable else deselectDrawable
        })
    }

    private fun initViewModel() {
        galleryViewModel = ViewModelProvider(
            requireActivity(),
            GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideGalleryViewModelFactory()
        )[GalleryViewModel::class.java].apply {
            hideSendOrCaptionLayout()
            viewLifecycleOwner.lifecycleScope.launch {
                mediaCountStateFlow.collect { setupMediaCountView(it) }
            }
        }

        bucketContentViewModel = ViewModelProvider(
            requireParentFragment(),
            GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketContentViewModelFactory()
        )[BucketContentViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            bucketContentViewModel.mediaList.collect {
                if (viewPagerMediaPreview == null)
                    viewPagerMediaPreview.adapter = mediaPreviewAdapter

                mediaPreviewAdapter.medias = it
                arguments?.getString("from_media_path", null).also { path ->
                    if (path != null) {
                        viewPagerMediaPreview.post {
                            viewPagerMediaPreview.setCurrentItem(bucketContentViewModel.getIndexOfPath(path).apply(this@PreviewFragment::checkForSelection), false)
                        }
                    }
                }
            }
        }
    }

    private fun setupMediaCountView(mediaCountModel: MediaCountModel) {
        if (mediaCountModel.selectedCount != 0 && mediaCountModel.totalCount != 0) {
            textViewMediaPreviewTitle.text = createMediaCountSpannable(
                context = requireContext(),
                value = mediaCountModel,
                colorAccent = GalleryActivityComponentHolder.getOrNull()?.provideGalleryStyleAttrs()?.galleryColorAccent ?: Color.BLUE
            )
        } else {
            textViewMediaPreviewTitle.text = ""
        }
    }

    private fun hideToolbarWithAnimation() {
        if (toolbarMediaPreview.visibility == View.GONE) return
        toolbarMediaPreview.startAnimation(TranslateAnimation(0f, 0f, 0f, -toolbarMediaPreview.height.toFloat()).apply {
            duration = 200
            fillAfter = true
            setOnAnimationEndListener {
                toolbarMediaPreview.visibility = View.GONE
                toolbarMediaPreview.animation = null
            }
        })
    }

    private fun showToolbarWithAnimation() {
        if (toolbarMediaPreview.visibility == View.VISIBLE) return
        toolbarMediaPreview.visibility = View.INVISIBLE
        toolbarMediaPreview.post {
            toolbarMediaPreview.startAnimation(TranslateAnimation(0f, 0f, -toolbarMediaPreview.height.toFloat(), 0f).apply {
                duration = 200
                fillAfter = true
                setOnAnimationEndListener {
                    toolbarMediaPreview.visibility = View.VISIBLE
                    toolbarMediaPreview.animation = null
                }
            })
        }
    }


    override fun onStop() {
        viewPagerMediaPreview.unregisterOnPageChangeCallback(pageSelectedCallback)
        super.onStop()
    }

    override fun onDestroyView() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        galleryViewModel.showSendOrCaptionLayout()
        galleryToolbarVisibilityController.showToolbar(false)
        viewPagerMediaPreview.adapter = null
        super.onDestroyView()
    }

    // onViewPager Click
    override fun onClick(v: View?) {
        if (toolbarMediaPreview.visibility == View.GONE)
            showToolbarWithAnimation()
        else
            hideToolbarWithAnimation()
    }
}