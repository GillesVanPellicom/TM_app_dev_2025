package com.example.movietracker.fragment.inspectTvShow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.movietracker.api.TvShowResponse

class TvShowViewModel : ViewModel() {
  private val _tvShow = MutableLiveData<TvShowResponse>()
  val tvShow: LiveData<TvShowResponse> get() = _tvShow

  fun setTvShow(tvShow: TvShowResponse) {
    _tvShow.value = tvShow
  }
}