package com.example.movietracker.fragment.filmsAndSeries

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
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
import com.example.movietracker.itemList.ItemAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FilmsAndSeriesFragment : Fragment(R.layout.fragment_films_and_series) {
    private lateinit var binding: FragmentFilmsAndSeriesBinding
    private val viewModel: FilmsAndSeriesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilmsAndSeriesBinding.bind(view)

        // Setup reload button behavior
        (requireActivity() as? MainActivity)?.setupRetryButton((requireActivity() as MainActivity).binding.reloadButton) {
            fetchTrendingMovies()
        }

        // Setup RecyclerView
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ItemAdapter(viewModel.movies) { selectedItem ->
            val bundle = Bundle().apply {
                putInt("id", selectedItem.tmbdId)
            }
            if (selectedItem.isFilm) {
                findNavController().navigate(R.id.action_inspect_movie, bundle)
            } else {
                findNavController().navigate(R.id.action_inspect_tv_show, bundle)
            }
        }
        binding.recyclerView.adapter = adapter

        // Add scroll listener for infinite scrolling
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!viewModel.isLoading && layoutManager.findLastVisibleItemPosition() == viewModel.movies.size - 1) {
                    fetchTrendingMovies()
                }
            }
        })

        // Fetch data if not already loaded
        if (viewModel.movies.isEmpty()) {
            fetchTrendingMovies()
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    private fun fetchTrendingMovies() {
        showLoadingSpinner()

        viewModel.fetchTrendingMovies(TmdbService.getApiKey(), TmdbService.create(), { newMovies ->
            val startPosition = viewModel.movies.size - newMovies.size
            binding.recyclerView.adapter?.notifyItemRangeInserted(startPosition, newMovies.size)
            hideLoadingSpinner()
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