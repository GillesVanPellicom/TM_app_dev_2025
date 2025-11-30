package com.example.movietracker.fragment.inspectTvShow

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movietracker.MainActivity
import com.example.movietracker.R
import com.example.movietracker.api.TmdbService
import com.example.movietracker.api.TvShowResponse
import com.example.movietracker.databinding.FragmentInspectTvShowBinding
import com.example.movietracker.fragment.liked.toggleLikeStatus
import com.example.movietracker.ui.scheduleFabWiggle
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class InspectTvShowFragment : Fragment(R.layout.fragment_inspect_tv_show) {
  private lateinit var binding: FragmentInspectTvShowBinding
  private val viewModel: TvShowViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding = FragmentInspectTvShowBinding.bind(view)

    // Handle back button click
    binding.topAppBar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }

    // Set up ViewPager and TabLayout
    val adapter = TabAdapter(this)
    binding.viewPager.adapter = adapter
    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
      tab.text = when (position) {
        0 -> "Overview"
        1 -> "Details"
        else -> null
      }
    }.attach()

    // Get TV show ID and fetch details
    val id = arguments?.getInt("id") ?: -1
    Log.d("InspectTvShowFragment", "TV Show ID: $id")
    (requireActivity() as? MainActivity)?.setupRetryButton((requireActivity() as MainActivity).binding.reloadButton) {
      fetchTvShowDetails(id)
    }
    fetchTvShowDetails(id)

    // Observe TV show data
    viewModel.tvShow.observe(viewLifecycleOwner) { tvShow ->
      binding.tvShow = tvShow
      checkIfTvShowIsLiked(tvShow?.id)
    }

    // Add TV show to database when FAB is clicked
// InspectTvShowFragment.kt
// Replace the FAB onClick listener with this:
    binding.fabLikedTvShow.setOnClickListener {
      val tvShow = viewModel.tvShow.value
      if (tvShow != null) {
        toggleLikeStatus(
          fab = binding.fabLikedTvShow,
          tmbdId = tvShow.id,
          title = tvShow.name ?: "",
          imageUrl = "https://image.tmdb.org/t/p/w500${tvShow.posterPath}",
          date = tvShow.firstAirDate,
          isFilm = false,
          rootView = binding.root
        )
      }
    }

    // Nudge the FAB after 30s to hint its presence
    scheduleFabWiggle(binding.fabLikedTvShow, initialDelayMs = 30_000L)
  }

  private fun checkIfTvShowIsLiked(tvShowId: Int?) {
    if (tvShowId == null) return
    lifecycleScope.launch {
      val items = MainActivity.Companion.database.itemDao().getItemsByType(isFilm = false)
      val isLiked = items.any { it.tmbdId == tvShowId }
      if (isLiked) {
        binding.fabLikedTvShow.setImageResource(R.drawable.ic_liked_filled)
      } else {
        binding.fabLikedTvShow.setImageResource(R.drawable.ic_liked)
      }
    }
  }

  private fun fetchTvShowDetails(tvShowId: Int) {
    showLoadingSpinner()

    val service = TmdbService.create()

    service.getTvShow(tvShowId, TmdbService.getApiKey()).enqueue(object : Callback<TvShowResponse> {
      override fun onResponse(call: Call<TvShowResponse>, response: Response<TvShowResponse>) {
        hideLoadingSpinner()
        if (!isAdded) return
        if (response.isSuccessful) {
          val tvShow = response.body()
          Log.d("InspectTvShowFragment", "Response: ${response.body()}")
          Log.d("InspectTvShowFragment", "Response Code: ${response.code()}")
          tvShow?.let { viewModel.setTvShow(it) }
        }
      }

      override fun onFailure(call: Call<TvShowResponse>, t: Throwable) {
        hideLoadingSpinner()
        showReloadButton()
        showErrorDialog()
        if (!isAdded) return
      }
    })
  }

  private inner class TabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
      return when (position) {
        0 -> TvShowOverviewFragment()
        1 -> TvShowDetailsFragment()
        else -> throw IllegalStateException("Invalid position")
      }
    }
  }

  private fun showLoadingSpinner(hideBackground: Boolean = true) {
    (requireActivity() as? MainActivity)?.showLoadingSpinner(hideBackground)
  }

  private fun hideLoadingSpinner() {
    (requireActivity() as? MainActivity)?.hideLoadingSpinner()
  }

  private fun showReloadButton(hideBackground: Boolean = true) {
    (requireActivity() as? MainActivity)?.showReloadButton(hideBackground)
  }

  private fun showErrorDialog() {
    (requireActivity() as? MainActivity)?.showErrorDialog()
  }
}