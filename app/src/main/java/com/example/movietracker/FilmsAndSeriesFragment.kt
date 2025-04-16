package com.example.movietracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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
import com.example.movietracker.api.TmdbService
import com.example.movietracker.databinding.FragmentFilmsAndSeriesBinding
import com.example.movietracker.itemList.Item
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
            // Navigate to InspectMovieFragment with the selected movie's ID
            val bundle = Bundle().apply {
                putInt("id", selectedItem.tmbdId)
            }
            findNavController().navigate(R.id.action_inspect_movie, bundle)
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

        val apiKey = "140b81b85e8e8baf9d417e99a3c9ab7e"
        val service = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)

        viewModel.fetchTrendingMovies(apiKey, service, { newMovies ->
            val startPosition = viewModel.movies.size - newMovies.size
            binding.recyclerView.adapter?.notifyItemRangeInserted(startPosition, newMovies.size)
            hideLoadingSpinner()
        }, {
            hideLoadingSpinner()
            showReloadButton()
            showErrorDialog()
        })
    }

    private fun showLoadingSpinner() {
        (requireActivity() as? MainActivity)?.showLoadingSpinner()
    }

    private fun hideLoadingSpinner() {
        (requireActivity() as? MainActivity)?.hideLoadingSpinner()
    }

    private fun showReloadButton() {
        (requireActivity() as? MainActivity)?.showReloadButton()
    }

    private fun showErrorDialog() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Material_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        // Set full-screen layout parameters
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inflate a layout for Material 3 dialog content
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.dialog_fullscreen_error,
            null
        )
        dialog.setContentView(dialogView)

        // Configure views
        dialogView.findViewById<TextView>(R.id.dialog_title).text = "An issue occurred"
        dialogView.findViewById<TextView>(R.id.dialog_message).text =
            "Something went wrong while loading.\nThis could be due to a network issue.\nPlease check your internet connection, and if the problem persists, \nplease try again later."
        dialogView.findViewById<ImageView>(R.id.dialog_icon)
            .setImageResource(R.drawable.ic_error)
        dialogView.findViewById<Button>(R.id.dialog_button_ok).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}