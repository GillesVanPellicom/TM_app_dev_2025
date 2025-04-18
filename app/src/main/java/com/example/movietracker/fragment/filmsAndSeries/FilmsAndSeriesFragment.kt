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
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupInfiniteScroll(layoutManager: LinearLayoutManager) {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!viewModel.isLoading &&
                    !viewModel.isInSearchMode &&
                    layoutManager.findLastVisibleItemPosition() == viewModel.movies.size - 1
                ) {
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

            // Force UI update on the main thread
            binding.recyclerView.post {
                adapter.updateItems(viewModel.movies)

                // Make sure recyclerView is visible
                binding.recyclerView.visibility = View.VISIBLE

                // Log success to verify data loaded
                Log.d("FilmsAndSeriesFragment", "Updated adapter with ${viewModel.movies.size} items")
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