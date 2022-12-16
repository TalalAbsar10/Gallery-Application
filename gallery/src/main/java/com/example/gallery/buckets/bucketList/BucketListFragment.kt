package com.example.gallery.buckets.bucketList

import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.gallery.R
import com.example.gallery.main.di.GalleryActivityComponentHolder
import com.example.gallery.main.gallery.BucketRecyclerViewItemMode
import com.example.gallery.main.ui.GalleryViewModel
import com.example.gallery.utils.divideScreenToEqualPart
import com.example.gallery.utils.dpToPx
import com.example.gallery.utils.setOnAnimationEndListener
import kotlinx.android.synthetic.main.fragment_bucket_list.*
import kotlinx.coroutines.launch

internal class BucketListFragment : Fragment(R.layout.fragment_bucket_list) {

    private lateinit var bucketListViewModel: BucketListViewModel
    private lateinit var galleryViewModel: GalleryViewModel

    private val bucketAdapter by lazy { GalleryActivityComponentHolder.getOrNull()!!.provideBucketListAdapter() }
    private lateinit var gridLayoutManager: GridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridLayoutManager = GridLayoutManager(requireContext(), getSpanCountBasedOnRecyclerViewMode())
        initViewModel()
        initView()
    }

    private fun initView() {
        bucketAdapter.viewHolderId = GalleryActivityComponentHolder
            .getOrNull()?.provideGalleryOptions()?.bucketRecyclerViewItemMode?.value ?: R.layout.grid_bucket_item_view

        recyclerViewBuckets.apply {
            adapter = bucketAdapter
            layoutManager = gridLayoutManager
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        bucketAdapter.apply {
            onBucketClick = {
                galleryViewModel.openBucketWithId(it, bucketListViewModel.getBucketNameById(it))
            }

            getImageViewWidth = {
                if (recyclerViewBuckets.layoutManager is GridLayoutManager) {
                    (recyclerViewBuckets.layoutManager as GridLayoutManager).let { gridLayoutManager ->
                        val displayMetric = requireActivity().resources.displayMetrics
                        (((displayMetric.widthPixels - dpToPx(3) * (gridLayoutManager.spanCount - 1)) / gridLayoutManager.spanCount) * 0.5).toInt()
                    }
                } else {
                    dpToPx(70)
                }
            }
        }
    }

    private fun initViewModel() {
        galleryViewModel = ViewModelProvider(
            requireActivity(),
            GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideGalleryViewModelFactory()
        )[GalleryViewModel::class.java].apply {
            bucketRecycleViewModeLiveData.observe(viewLifecycleOwner) {
                changeRecyclerViewItemModeTo(it)
            }
        }

        bucketListViewModel = ViewModelProvider(
            this,
            GalleryActivityComponentHolder.createOrGetComponent(requireActivity()).provideBucketListViewModelFactory()
        )[BucketListViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                galleryViewModel.storagePermissionGrantedStateFlow.collect {
                    if (it == true) {
                        bucketListViewModel.getBuckets()
                    }
                }
            }
        }

        bucketListViewModel.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                loadingViewStateFlow.collect {
                    when (it) {
                        is LoadingViewState.Error -> showErrorLayout()
                        is LoadingViewState.ShowLoading -> showLoading()
                        is LoadingViewState.HideLoading -> hideLoading()
                        else -> {}
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                launch { allMediaCountChanged.collect { galleryViewModel.totalMediaCount = it } }
                bucketsStateFlow.collect { bucketAdapter.submitList(it) }
            }
        }
    }

    private fun changeRecyclerViewItemModeTo(bucketRecyclerViewItemMode: BucketRecyclerViewItemMode) {
        if (recyclerViewBuckets.layoutManager is GridLayoutManager) {
            bucketAdapter.viewHolderId = bucketRecyclerViewItemMode.value
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) TransitionManager.beginDelayedTransition(recyclerViewBuckets)
            (recyclerViewBuckets.layoutManager as GridLayoutManager).spanCount = getSpanCountBasedOnRecyclerViewMode()
        }
    }


    private fun showErrorLayout() {
        hideLoading()
        recyclerViewBuckets.visibility = View.GONE
        errorLayoutBucketList.show()
        errorLayoutBucketList.setOnRetryClickListener { bucketListViewModel.retry() }
    }


    private fun showLoading() {
        errorLayoutBucketList.hide()
        contentLoadingProgressBarBucketList.visibility = View.VISIBLE
        if (recyclerViewBuckets.visibility == View.VISIBLE) {
            recyclerViewBuckets.startAnimation(
                AlphaAnimation(1f, 0.5f).apply {
                    duration = 250
                    setOnAnimationEndListener {
                        recyclerViewBuckets.visibility = View.GONE
                    }
                }
            )
        }
    }

    private fun hideLoading() {
        errorLayoutBucketList.hide()
        contentLoadingProgressBarBucketList.visibility = View.GONE
        recyclerViewBuckets.visibility = View.INVISIBLE
        recyclerViewBuckets.startAnimation(
            AlphaAnimation(0.5f, 1f).apply {
                duration = 250
                setOnAnimationEndListener {
                    recyclerViewBuckets.visibility = View.VISIBLE
                }
            }
        )
    }

    private fun getSpanCountBasedOnRecyclerViewMode(): Int = if (GalleryActivityComponentHolder.getOrNull()?.provideGalleryOptions()?.bucketRecyclerViewItemMode ==
        BucketRecyclerViewItemMode.GridStyle
    ) {
        divideScreenToEqualPart(
            displayWidth = resources.displayMetrics.widthPixels,
            itemWidth = resources.getDimension(R.dimen.gallery_bucket_max_width),
            minCount = 2
        )
    } else 1

    override fun onDestroyView() {
        recyclerViewBuckets?.adapter = null
        recyclerViewBuckets?.layoutManager = null
        super.onDestroyView()
    }
}