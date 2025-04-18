package com.example.movietracker.fragment.liked

import android.os.Bundle
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
import com.example.movietracker.databinding.FragmentLikedBinding
import com.example.movietracker.itemList.Item
import com.example.movietracker.itemList.ItemAdapter
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class LikedFragment : Fragment(R.layout.fragment_liked) {
  private lateinit var binding: FragmentLikedBinding
  private val viewModel: LikedViewModel by viewModels()
  private lateinit var adapter: ItemAdapter
  private lateinit var searchAdapter: ItemAdapter

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding = FragmentLikedBinding.bind(view)

    // Setup RecyclerView
    setupRecyclerView()

    // Setup Search
    setupSearch()

    // Load items from database
    loadLikedItems()
  }

  override fun onResume() {
    super.onResume()
    // Refresh data when returning to fragment
    loadLikedItems()
  }

  private fun setupRecyclerView() {
    val layoutManager = LinearLayoutManager(context)
    binding.recyclerView.layoutManager = layoutManager

    // Initialize adapter with empty list
    adapter = ItemAdapter(emptyList()) { selectedItem ->
      navigateToItemDetail(selectedItem)
    }
    binding.recyclerView.adapter = adapter
  }

  private fun setupSearch() {
    // Set up search bar with search view
    binding.searchView.setupWithSearchBar(binding.searchBar)

    // Set up search results recycler view
    val searchLayoutManager = LinearLayoutManager(context)
    binding.searchView.findViewById<RecyclerView>(R.id.searchResultsRecyclerView).apply {
      layoutManager = searchLayoutManager
      searchAdapter = ItemAdapter(emptyList()) { selectedItem ->
        binding.searchView.hide()
        navigateToItemDetail(selectedItem)
      }
      adapter = searchAdapter
    }

    // Set up search query listener
    binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        val query = binding.searchView.editText.text.toString()
        if (query.isNotBlank()) {
          viewModel.currentSearchQuery = query
          performSearch(query)
        }
        true
      } else {
        false
      }
    }

    // Handle SearchView state changes
    binding.searchView.addTransitionListener { _, _, newState ->
      viewModel.isSearchViewActive = newState == SearchView.TransitionState.SHOWING ||
          newState == SearchView.TransitionState.SHOWN

      // When search view is hidden, reset search mode
      if (newState == SearchView.TransitionState.HIDDEN && viewModel.isInSearchMode) {
        viewModel.isInSearchMode = false
        adapter.updateItems(viewModel.items)
      }
    }

    // Restore search state if needed
    restoreSearchState()
  }

  private fun restoreSearchState() {
    if (viewModel.isSearchViewActive) {
      binding.searchBar.setText(viewModel.currentSearchQuery)
      binding.searchView.show()

      if (viewModel.isInSearchMode && viewModel.searchResults.isNotEmpty()) {
        searchAdapter.updateItems(viewModel.searchResults)
      }
    }
  }

  private fun loadLikedItems() {
    lifecycleScope.launch {
      try {
        // Get all items ordered by creation date desc
        val likedItems = MainActivity.database.itemDao().getAllItems()

        // Update view model data
        viewModel.items.clear()
        viewModel.items.addAll(likedItems)

        // Update UI
        updateUI()
      } catch (e: Exception) {
        // Handle error
        binding.emptyStateText.text = "Error loading liked items"
        binding.emptyStateText.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
      }
    }
  }

  private fun updateUI() {
    if (viewModel.items.isEmpty()) {
      binding.emptyStateText.visibility = View.VISIBLE
      binding.recyclerView.visibility = View.GONE
    } else {
      binding.emptyStateText.visibility = View.GONE
      binding.recyclerView.visibility = View.VISIBLE
      adapter.updateItems(viewModel.items)
    }
  }

  private fun performSearch(query: String) {
    viewModel.isInSearchMode = true
    val filteredItems = viewModel.filterItems(query)
    viewModel.searchResults.clear()
    viewModel.searchResults.addAll(filteredItems)
    searchAdapter.updateItems(filteredItems)
  }

  private fun navigateToItemDetail(selectedItem: Item) {
    val bundle = Bundle().apply {
      putInt("id", selectedItem.tmbdId)
      putInt("sourceTabId", R.id.nav_liked)
    }
    if (selectedItem.isFilm) {
      findNavController().navigate(R.id.action_inspect_movie, bundle)
    } else {
      findNavController().navigate(R.id.action_inspect_tv_show, bundle)
    }
  }
}