package com.example.movietracker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.movietracker.databinding.FragmentFilmsAndSeriesBinding

class FilmsAndSeriesFragment : Fragment(R.layout.fragment_films_and_series) {
    private lateinit var binding: FragmentFilmsAndSeriesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilmsAndSeriesBinding.bind(view)
    }
}