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
}