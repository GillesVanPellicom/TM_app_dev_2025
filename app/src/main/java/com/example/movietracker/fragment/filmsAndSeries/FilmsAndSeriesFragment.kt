package com.example.movietracker.fragment.filmsAndSeries

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch

class FilmsAndSeriesFragment : Fragment(R.layout.fragment_films_and_series) {
  private lateinit var repository: com.example.movietracker.repository.MovieRepository
  private var filterShowMovies: Boolean = true
  private var filterShowTv: Boolean = true
  private lateinit var binding: FragmentFilmsAndSeriesBinding
  private val viewModel: FilmsAndSeriesViewModel by viewModels()
  private lateinit var adapter: ItemAdapter
  private lateinit var searchAdapter: ItemAdapter
  private var recyclerViewState: Parcelable? = null
  private var searchRecyclerViewState: Parcelable? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // Init repository for cache-first data access
    val dao = MainActivity.database.cachedItemDao()
    repository = com.example.movietracker.repository.MovieRepository(
      cachedItemDao = dao,
      service = TmdbService.create(),
      apiKey = TmdbService.getApiKey()
    )
    super.onViewCreated(view, savedInstanceState)
    binding = FragmentFilmsAndSeriesBinding.bind(view)

    // Setup RecyclerView first

    // Setup chip filters
    binding.chipMovies.setOnCheckedChangeListener { _, isChecked ->
      filterShowMovies = isChecked
      applyAndRenderFilters()
    }
    binding.chipTv.setOnCheckedChangeListener { _, isChecked ->
      filterShowTv = isChecked
      applyAndRenderFilters()
    }

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

    // Restore scroll state if available using new API (API 33+)
    if (savedInstanceState != null) {
      recyclerViewState = savedInstanceState.getParcelable("RECYCLER_VIEW_STATE", Parcelable::class.java)
      searchRecyclerViewState = savedInstanceState.getParcelable("SEARCH_RECYCLER_VIEW_STATE", Parcelable::class.java)
    }

    // Set up search
    setupSearch()

    // Setup infinite scroll
    setupInfiniteScroll(layoutManager)

    // Restore data to adapters (if already fetched)
    if (viewModel.movies.isNotEmpty()) {
      adapter.updateItems(viewModel.movies)
      // Restore scroll position after data is set
      recyclerViewState?.let { binding.recyclerView.layoutManager?.onRestoreInstanceState(it) }
    } else {
      fetchTrendingMovies()
    }
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

    // Set up search searchQuery listener
    binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        val query = binding.searchView.editText.text.toString()
        if (query.isNotBlank()) {
          // Save searchQuery in ViewModel
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
        // Restore search recycler view position
        val searchRecyclerView = binding.searchView.findViewById<RecyclerView>(R.id.searchResultsRecyclerView)
        searchRecyclerViewState?.let { searchRecyclerView.layoutManager?.onRestoreInstanceState(it) }
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
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val results = repository.search(query)
        hideLoadingSpinner()
        viewModel.searchResults.clear()
        viewModel.searchResults.addAll(applyFilters(results))
        searchAdapter.updateItems(viewModel.searchResults)
      } catch (e: Exception) {
        hideLoadingSpinner()
        showErrorDialog()
      }
    }
  }

  private fun navigateToItemDetail(selectedItem: Item) {
    val bundle = Bundle().apply {
      putInt("id", selectedItem.tmbdId)
      putInt("sourceTabId", R.id.nav_films_and_series)
    }
    if (selectedItem.isFilm) {
      findNavController().navigate(R.id.action_inspect_movie, bundle)
    } else {
      findNavController().navigate(R.id.action_inspect_tv_show, bundle)
    }
  }

  private fun fetchTrendingMovies() {
    showLoadingSpinner()
    viewLifecycleOwner.lifecycleScope.launch {
      try {
        val newItems = repository.getTrending(viewModel.currentPage)
        // Update ViewModel state similar to original
        viewModel.movies.addAll(newItems)
        viewModel.currentPage++
        hideLoadingSpinner()
        binding.recyclerView.post {
          if (isAdded) {
            val updatedList = ArrayList(applyFilters(viewModel.movies))
            adapter.updateItems(updatedList)
            binding.recyclerView.requestLayout()
            Log.d("FilmsAndSeriesFragment", "Updated adapter with ${updatedList.size} items (filtered)")
          }
        }
      } catch (e: Exception) {
        hideLoadingSpinner()
        showReloadButton()
        showErrorDialog()
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    // Only try to save state if binding is initialized
    if (::binding.isInitialized) {
      // Save main RecyclerView state
      recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()
      outState.putParcelable("RECYCLER_VIEW_STATE", recyclerViewState)

      // Save search RecyclerView state
      val searchRecyclerView = binding.searchView.findViewById<RecyclerView>(R.id.searchResultsRecyclerView)
      searchRecyclerViewState = searchRecyclerView.layoutManager?.onSaveInstanceState()
      outState.putParcelable("SEARCH_RECYCLER_VIEW_STATE", searchRecyclerViewState)
    }
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
  private fun applyFilters(source: List<Item>): List<Item> {
    val filteredByType = source.filter { item ->
      when {
        item.isFilm && filterShowMovies -> true
        !item.isFilm && filterShowTv -> true
        else -> false
      }
    }
    return filteredByType
  }

  private fun applyAndRenderFilters() {
    if (::adapter.isInitialized) {
      val listToShow = if (viewModel.isInSearchMode) viewModel.searchResults else viewModel.movies
      val filtered = applyFilters(listToShow)
      adapter.updateItems(filtered)
    }
    if (::searchAdapter.isInitialized && viewModel.isInSearchMode) {
      val filteredSearch = applyFilters(viewModel.searchResults)
      searchAdapter.updateItems(filteredSearch)
    }
  }
}