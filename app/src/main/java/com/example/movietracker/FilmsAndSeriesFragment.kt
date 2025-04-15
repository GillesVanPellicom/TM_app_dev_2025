package com.example.movietracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movietracker.api.TmdbService
import com.example.movietracker.api.TrendingResponse
import com.example.movietracker.databinding.FragmentFilmsAndSeriesBinding
import com.example.movietracker.itemList.Item
import com.example.movietracker.itemList.ItemAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.recyclerview.widget.RecyclerView

class FilmsAndSeriesFragment : Fragment(R.layout.fragment_films_and_series) {
    private lateinit var binding: FragmentFilmsAndSeriesBinding
    private val movies = mutableListOf<Item>()
    private var currentPage = 1
    private var isLoading = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilmsAndSeriesBinding.bind(view)

        // Setup RecyclerView
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ItemAdapter(movies)
        binding.recyclerView.adapter = adapter

        // Add scroll listener for infinite scrolling
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && layoutManager.findLastVisibleItemPosition() == movies.size - 1) {
                    currentPage++
                    fetchTrendingMovies(currentPage)
                }
            }
        })

        // Fetch the first page of trending movies
        fetchTrendingMovies(currentPage)
    }

    private fun fetchTrendingMovies(page: Int) {
        isLoading = true
        val apiKey = "140b81b85e8e8baf9d417e99a3c9ab7e"
        val service = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)

        service.getTrending(apiKey, page).enqueue(object : Callback<TrendingResponse> {
            override fun onResponse(call: Call<TrendingResponse>, response: Response<TrendingResponse>) {
                if (response.isSuccessful) {
                    val newMovies = response.body()?.results?.map {
                        Item(
                            id = it.id,
                            title = it.title ?: it.name.orEmpty(),
                            subTitle = it.formattedReleaseInfo ?: "Release info unavailable",
                            imageUrl = "https://image.tmdb.org/t/p/w500${it.posterPath}"
                        )
                    } ?: emptyList()

                    val startPosition = movies.size
                    movies.addAll(newMovies)
                    binding.recyclerView.adapter?.notifyItemRangeInserted(startPosition, newMovies.size)
                }
                isLoading = false
            }

            override fun onFailure(call: Call<TrendingResponse>, t: Throwable) {
                // Handle error (e.g., show a Toast or log the error)
                isLoading = false
            }
        })
    }
}