package com.example.movietracker.fragment.inspectMovie

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movietracker.MainActivity
import com.example.movietracker.R
import com.example.movietracker.api.MovieResponse
import com.example.movietracker.api.TmdbService
import com.example.movietracker.databinding.FragmentInspectMovieBinding
import com.example.movietracker.itemList.Item
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InspectMovieFragment : Fragment(R.layout.fragment_inspect_movie) {
    private lateinit var binding: FragmentInspectMovieBinding
    private val viewModel: MovieViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInspectMovieBinding.bind(view)

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

        // Get movie ID and fetch details
        val id = arguments?.getInt("id") ?: -1
        Log.d("InspectMovieFragment", "Movie ID: $id")
        (requireActivity() as? MainActivity)?.setupRetryButton((requireActivity() as MainActivity).binding.reloadButton) {
            fetchMovieDetails(id)
        }

        fetchMovieDetails(id)

        // Observe movie data
        viewModel.movie.observe(viewLifecycleOwner) { movie ->
            binding.movie = movie
            checkIfMovieIsLiked(movie?.id)
        }

        // Add movie to database when FAB is clicked
        binding.fabLikedMovie.setOnClickListener {
            val movie = viewModel.movie.value
            if (movie != null) {
                lifecycleScope.launch {
                    val items =
                        MainActivity.Companion.database.itemDao().getItemsByType(isFilm = true)
                    val existingItem = items.find { it.tmbdId == movie.id }

                    if (existingItem != null) {
                        // Remove the movie from the database
                        MainActivity.Companion.database.itemDao().delete(existingItem)
                        Log.d("InspectMovieFragment", "Movie removed from database: $existingItem")
                        binding.fabLikedMovie.setImageResource(R.drawable.ic_liked)
                    } else {
                        // Add the movie to the database
                        val item = Item(
                            tmbdId = movie.id,
                            imageUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            title = movie.title ?: "Error",
                            subTitle = movie.releaseDate?.take(4) ?: "Error",
                            isFilm = true
                        )
                        MainActivity.Companion.database.itemDao().insert(item)
                        Log.d("InspectMovieFragment", "Movie added to database: $item")
                        binding.fabLikedMovie.setImageResource(R.drawable.ic_liked_filled)
                    }
                }
            }
        }
    }

    private fun checkIfMovieIsLiked(movieId: Int?) {
        if (movieId == null) return
        lifecycleScope.launch {
            val items = MainActivity.Companion.database.itemDao().getItemsByType(isFilm = true)
            val isLiked = items.any { it.tmbdId == movieId }
            if (isLiked) {
                binding.fabLikedMovie.setImageResource(R.drawable.ic_liked_filled)
            } else {
                binding.fabLikedMovie.setImageResource(R.drawable.ic_liked)
            }
        }
    }

    private fun fetchMovieDetails(movieId: Int) {
        showLoadingSpinner()

        val apiKey = "140b81b85e8e8baf9d417e99a3c9ab7e"
        val service = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)

        service.getMovie(movieId, apiKey).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                hideLoadingSpinner()
                if (!isAdded) return // Ensure the fragment is still attached
                if (response.isSuccessful) {
                    val movie = response.body()
                    Log.d("InspectMovieFragment", "Response: ${response.body()}")
                    Log.d("InspectMovieFragment", "Response Code: ${response.code()}")
                    movie?.let {
                        viewModel.setMovie(it)
                    }
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                hideLoadingSpinner()
                showReloadButton()

                if (!isAdded) return // Ensure the fragment is still attached

                showErrorDialog()
            }
        })
    }

    private inner class TabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> MovieOverviewFragment()
                1 -> MovieDetailsFragment()
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