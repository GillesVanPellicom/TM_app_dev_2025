package com.example.movietracker.fragment.liked

import androidx.lifecycle.ViewModel
import com.example.movietracker.itemList.Item

class LikedViewModel : ViewModel() {
    // All liked items from database
    val items = mutableListOf<Item>()

    // Search related variables
    var isInSearchMode = false
    var currentSearchQuery: String = ""
    var isSearchViewActive: Boolean = false
    var searchResults = mutableListOf<Item>()

    // Filter items based on search query
    fun filterItems(query: String): List<Item> {
        val lowercaseQuery = query.lowercase()
        return items.filter {
            it.title.lowercase().contains(lowercaseQuery) ||
                    it.subTitle.lowercase().contains(lowercaseQuery)
        }
    }
}