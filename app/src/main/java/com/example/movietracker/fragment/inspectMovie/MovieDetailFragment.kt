package com.example.movietracker.fragment.inspectMovie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movietracker.databinding.FragmentMovieDetailsBinding

class MovieDetailsFragment : Fragment() {
  private lateinit var binding: FragmentMovieDetailsBinding
  private val viewModel: MovieViewModel by viewModels({ requireParentFragment() })

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.movie.observe(viewLifecycleOwner) { movie ->
      binding.movie = movie
    }
  }
}