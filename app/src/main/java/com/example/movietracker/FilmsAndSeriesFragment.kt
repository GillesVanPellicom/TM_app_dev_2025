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
import androidx.navigation.fragment.findNavController
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

    private fun showLoadingSpinner() {
        (requireActivity() as? MainActivity)?.showLoadingSpinner()
        isLoading = true
    }

    private fun hideLoadingSpinner() {
        (requireActivity() as? MainActivity)?.hideLoadingSpinner()
        isLoading = false
    }

    private fun showReloadButton() {
        (requireActivity() as? MainActivity)?.showReloadButton()
    }

    private fun hideReloadButton() {
        (requireActivity() as? MainActivity)?.hideReloadButton()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilmsAndSeriesBinding.bind(view)

        // Setup reload button behavior
        (requireActivity() as? MainActivity)?.setupRetryButton((requireActivity() as MainActivity).binding.reloadButton) {
            fetchTrendingMovies(currentPage)
        }

        // Setup RecyclerView
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ItemAdapter(movies) { selectedItem ->
            // Navigate to InspectMovieFragment with the selected movie's ID
            val bundle = Bundle().apply {
                putInt("id", selectedItem.id)
            }
            findNavController().navigate(R.id.action_inspect_movie, bundle)
        }
        binding.recyclerView.adapter = adapter

        // Add scroll listener for infinite scrolling
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading && layoutManager.findLastVisibleItemPosition() == movies.size - 1) {
                    showLoadingSpinner()
                    currentPage++
                    fetchTrendingMovies(currentPage)
                }
            }
        })

        // Fetch the first page of trending movies
        fetchTrendingMovies(currentPage)
    }

    private fun fetchTrendingMovies(page: Int) {
        showLoadingSpinner()

        val apiKey = "140b81b85e8e8baf9d417e99a3c9ab7e"
        val service = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbService::class.java)

        service.getTrending(apiKey, page).enqueue(object : Callback<TrendingResponse> {
            override fun onResponse(
                call: Call<TrendingResponse>,
                response: Response<TrendingResponse>
            ) {
                hideLoadingSpinner()
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
                    binding.recyclerView.adapter?.notifyItemRangeInserted(
                        startPosition,
                        newMovies.size
                    )
                }
            }

            override fun onFailure(call: Call<TrendingResponse>, t: Throwable) {
                hideLoadingSpinner()
                showReloadButton()

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
        })
    }
}