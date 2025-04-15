package com.example.movietracker

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
import androidx.navigation.fragment.findNavController
import com.example.movietracker.api.MovieResponse
import com.example.movietracker.api.TmdbService
import com.example.movietracker.databinding.FragmentInspectMovieBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InspectMovieFragment : Fragment(R.layout.fragment_inspect_movie) {
    private lateinit var binding: FragmentInspectMovieBinding

    private fun showLoadingSpinner() {
        (requireActivity() as? MainActivity)?.showLoadingSpinner()
    }

    private fun hideLoadingSpinner() {
        (requireActivity() as? MainActivity)?.hideLoadingSpinner()
    }

    private fun showReloadButton() {
        (requireActivity() as? MainActivity)?.showReloadButton()
    }

    private fun hideReloadButton() {
        (requireActivity() as? MainActivity)?.hideReloadButton()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInspectMovieBinding.bind(view)



        // Handle back button click
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
//        binding.topAppBar.setTitle()

        // Get movie ID from arguments
        val id = arguments?.getInt("id") ?: -1

        // Setup reload button behavior
        (requireActivity() as? MainActivity)?.setupRetryButton((requireActivity() as MainActivity).binding.reloadButton) {
            fetchMovieDetails(id)
        }

        fetchMovieDetails(id)

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
                if (response.isSuccessful) {
                    val movie = response.body()
                    Log.d("InspectMovieFragment", "Response: ${response.body()}")
                    Log.d("InspectMovieFragment", "Response Code: ${response.code()}")
                    binding.textViewMovieId.text = movie?.title
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
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