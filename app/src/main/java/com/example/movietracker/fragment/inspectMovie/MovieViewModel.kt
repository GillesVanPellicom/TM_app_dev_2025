package com.example.movietracker.fragment.inspectMovie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.movietracker.api.MovieResponse

class MovieViewModel : ViewModel() {
  private val _movie = MutableLiveData<MovieResponse>()
  val movie: LiveData<MovieResponse> get() = _movie

  fun setMovie(movie: MovieResponse) {
    _movie.value = movie
  }
}