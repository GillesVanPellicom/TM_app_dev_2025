package com.example.movietracker.fragment.filmsAndSeries

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movietracker.MainActivity
import com.example.movietracker.R
import com.example.movietracker.api.TmdbService
import com.example.movietracker.databinding.FragmentFilmsAndSeriesBinding
import com.example.movietracker.itemList.Item
import com.example.movietracker.itemList.ItemAdapter
import com.google.android.material.search.SearchView
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.text.clear
import kotlin.text.compareTo
import kotlin.toString

class FilmsAndSeriesFragment : Fragment(R.layout.fragment_films_and_series) {
    private lateinit var binding: FragmentFilmsAndSeriesBinding
    private val viewModel: FilmsAndSeriesViewModel by viewModels()
    private lateinit var adapter: ItemAdapter
    private lateinit var searchAdapter: ItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilmsAndSeriesBinding.bind(view)

        // Setup RecyclerView first
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager

        // Initialize adapter with empty list to prevent null adapter issues
        adapter = ItemAdapter(emptyList()) { selectedItem ->
            navigateToItemDetail(selectedItem)
        }
        binding.recyclerView.adapter = adapter

        // Setup retry button
        (requireActivity() as? MainActivity)?.setupRetryButton(
            (requireActivity() as MainActivity).binding.reloadButton
        ) { fetchTrendingMovies() }

        // Set up search
        setupSearch()

        // Setup infinite scroll
        setupInfiniteScroll(layoutManager)

        // Always fetch initial data - remove the check for empty movies list
        fetchTrendingMovies()
    }

    private fun setupSearch() {
        // Set up search bar with search view
        binding.searchView.setupWithSearchBar(binding.searchBar)

        // Set up search results recycler view
        val searchLayoutManager = LinearLayoutManager(context)
        binding.searchView.findViewById<RecyclerView>(R.id.searchResultsRecyclerView).apply {
            layoutManager = searchLayoutManager
            // Create a separate adapter for search results
            val searchAdapter = ItemAdapter(emptyList()) { selectedItem ->
                // Close search view when item is selected
                binding.searchView.hide()
                navigateToItemDetail(selectedItem)
            }
            adapter = searchAdapter

            // Save reference to search adapter
            this@FilmsAndSeriesFragment.searchAdapter = searchAdapter
        }

        // Set up search query listener
        binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.editText.text.toString()
                if (query.isNotBlank()) {
                    // Save query in ViewModel
                    viewModel.currentSearchQuery = query
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }

        // Handle SearchView state changes
        binding.searchView.addTransitionListener { _, previousState, newState ->
            // Update search state in ViewModel
            viewModel.isSearchViewActive = newState == SearchView.TransitionState.SHOWING ||
                    newState == SearchView.TransitionState.SHOWN

            // When search view is hidden, properly reset search state
            if (newState == SearchView.TransitionState.HIDDEN) {
                viewModel.isInSearchMode = false
                adapter.updateItems(viewModel.movies)

                // Clear search results when exiting search
                viewModel.searchResults.clear()

                Log.d("FilmsAndSeriesFragment", "Search dismissed - resetting search mode")
            }
        }

        // Restore search state after configuration change
        restoreSearchState()
    }

    private fun restoreSearchState() {
        if (viewModel.isSearchViewActive) {
            binding.searchBar.setText(viewModel.currentSearchQuery)

            // Make sure search mode is properly set based on whether we have results
            viewModel.isInSearchMode = viewModel.searchResults.isNotEmpty()

            binding.searchView.show()

            if (viewModel.searchResults.isNotEmpty()) {
                searchAdapter.updateItems(viewModel.searchResults)
            }
        } else {
            // Ensure we're not in search mode when not searching
            viewModel.isInSearchMode = false
        }
    }

    private fun setupInfiniteScroll(layoutManager: LinearLayoutManager) {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                // Load when approaching the end (3 items before) instead of exactly at the end
                val shouldLoad = !viewModel.isLoading &&
                        !viewModel.isInSearchMode &&
                        lastVisiblePosition >= viewModel.movies.size - 3

                if (shouldLoad) {
                    Log.d("FilmsAndSeriesFragment", "Loading more trending items")
                    fetchTrendingMovies()
                }
            }
        })
    }

    private fun performSearch(query: String) {
        showLoadingSpinner(true)
        viewModel.searchMoviesOrShows(
            query, TmdbService.getApiKey(), TmdbService.create(),
            onSuccess = { searchResults ->
                hideLoadingSpinner()
                // Store results in ViewModel for configuration changes
                viewModel.searchResults.clear()
                viewModel.searchResults.addAll(searchResults)
                // Update the search adapter with results
                searchAdapter.updateItems(searchResults)
            },
            onFailure = {
                hideLoadingSpinner()
                showErrorDialog()
            }
        )
    }

    private fun navigateToItemDetail(selectedItem: Item) {
        val bundle = Bundle().apply {
            putInt("id", selectedItem.tmbdId)
        }
        if (selectedItem.isFilm) {
            findNavController().navigate(R.id.action_inspect_movie, bundle)
        } else {
            findNavController().navigate(R.id.action_inspect_tv_show, bundle)
        }
    }

    private fun fetchTrendingMovies() {
        showLoadingSpinner()

        viewModel.fetchTrendingMovies(TmdbService.getApiKey(), TmdbService.create(), { newMovies ->
            hideLoadingSpinner()

            // Use a handler to ensure the UI update happens after the current frame is drawn
            binding.recyclerView.post {
                if (isAdded) {
                    // Create a copy of the list to ensure adapter recognizes it as new data
                    val updatedList = ArrayList(viewModel.movies)
                    adapter.updateItems(updatedList)

                    // Request layout to ensure scrolling calculations are updated
                    binding.recyclerView.requestLayout()

                    Log.d("FilmsAndSeriesFragment", "Updated adapter with ${viewModel.movies.size} items")
                }
            }
        }, {
            hideLoadingSpinner()
            showReloadButton()
            showErrorDialog()
        })
    }

    private fun showLoadingSpinner(hideBackground: Boolean = false) {
        (requireActivity() as? MainActivity)?.showLoadingSpinner(hideBackground)
    }

    private fun hideLoadingSpinner() {
        (requireActivity() as? MainActivity)?.hideLoadingSpinner()
    }

    private fun showReloadButton(hideBackground: Boolean = false) {
        (requireActivity() as? MainActivity)?.showReloadButton(hideBackground)
    }

    private fun showErrorDialog() {
        (requireActivity() as? MainActivity)?.showErrorDialog()
    }
}