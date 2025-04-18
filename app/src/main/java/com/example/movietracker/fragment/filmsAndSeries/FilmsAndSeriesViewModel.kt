package com.example.movietracker.fragment.filmsAndSeries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietracker.api.TmdbService
import com.example.movietracker.api.TrendingResponse
import com.example.movietracker.itemList.Item
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilmsAndSeriesViewModel : ViewModel() {
  val movies = mutableListOf<Item>()
  var currentPage = 1
  var isLoading = false
  var isInSearchMode = false

  // Search persistence variables
  var currentSearchQuery: String = ""
  var isSearchViewActive: Boolean = false
  var searchResults = mutableListOf<Item>()

  fun fetchTrendingMovies(
    apiKey: String,
    service: TmdbService,
    onSuccess: (List<Item>) -> Unit,
    onFailure: () -> Unit
  ) {
    if (isLoading) return
    isLoading = true

    viewModelScope.launch {
      service.getTrending(apiKey, currentPage).enqueue(object : Callback<TrendingResponse> {
        override fun onResponse(
          call: Call<TrendingResponse>,
          response: Response<TrendingResponse>
        ) {
          isLoading = false
          if (response.isSuccessful) {

            val newMovies = response.body()?.results?.map {
              Item(
                tmbdId = it.id,
                title = it.title ?: it.name.orEmpty(),
                subTitle = it.formattedReleaseInfo ?: "Release info unavailable",
                imageUrl = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                isFilm = it.mediaType == "movie"
              )
            } ?: emptyList()
            movies.addAll(newMovies)
            currentPage++
            onSuccess(newMovies)
          } else {
            onFailure()
          }
        }

        override fun onFailure(call: Call<TrendingResponse>, t: Throwable) {
          isLoading = false
          onFailure()
        }
      })
    }
  }


  fun searchMoviesOrShows(
    query: String,
    apiKey: String,
    service: TmdbService,
    onSuccess: (List<Item>) -> Unit,
    onFailure: () -> Unit
  ) {
    if (isLoading) return
    isLoading = true
    isInSearchMode = true

    viewModelScope.launch {
      // Add language parameter for better relevance
      service.searchMoviesOrShows(apiKey, query, 1, "en-US")
        .enqueue(object : Callback<TrendingResponse> {
          override fun onResponse(
            call: Call<TrendingResponse>,
            response: Response<TrendingResponse>
          ) {
            isLoading = false
            if (response.isSuccessful) {
              val searchResults = response.body()?.results
                // Sort by popularity in descending order
                ?.sortedByDescending { it.popularity }
                ?.map {
                  Item(
                    tmbdId = it.id,
                    title = it.title ?: it.name.orEmpty(),
                    subTitle = it.formattedReleaseInfo
                      ?: "Release info unavailable",
                    imageUrl = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                    isFilm = it.mediaType == "movie"
                  )
                } ?: emptyList()

              onSuccess(searchResults)
            } else {
              onFailure()
              isInSearchMode = false
            }
          }

          override fun onFailure(call: Call<TrendingResponse>, t: Throwable) {
            isLoading = false
            isInSearchMode = false
            onFailure()
          }
        })
    }
  }
}