package com.example.movietracker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.movietracker.api.Genre
import com.example.movietracker.api.MovieResponse
import com.example.movietracker.fragment.inspectMovie.MovieViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class MovieViewModelTest {

  // Ensures LiveData posts happen synchronously
  @get:Rule
  val instantTaskExecutorRule = InstantTaskExecutorRule()

  @Test
  fun setMovie_updatesLiveData_withFakeDataSource() {
    val viewModel = MovieViewModel()
    val fakeMovie = FakeMovieDataSource().getMovie()

    viewModel.setMovie(fakeMovie)

    val value = viewModel.movie.getOrAwaitValue()
    assertEquals(fakeMovie.id, value.id)
    assertEquals(fakeMovie.title, value.title)
  }

  // Simple fake data source to supply a MovieResponse
  class FakeMovieDataSource {
    fun getMovie(): MovieResponse = MovieResponse(
      adult = false,
      backdropPath = null,
      belongsToCollection = null,
      budget = 0,
      genres = listOf(Genre(1, "Action")),
      homepage = null,
      id = 123,
      imdbId = null,
      originalLanguage = "en",
      originalTitle = "Original",
      overview = "Overview",
      popularity = 1.0,
      posterPath = "/path.jpg",
      productionCompanies = emptyList(),
      productionCountries = emptyList(),
      releaseDate = "2024-10-10",
      revenue = 0,
      runtime = 120,
      spokenLanguages = emptyList(),
      status = "Released",
      tagline = null,
      title = "Fake Movie",
      video = false,
      voteAverage = 7.5,
      voteCount = 100
    )
  }
}

// LiveData helper used only in unit tests
private fun <T> androidx.lifecycle.LiveData<T>.getOrAwaitValue(
  time: Long = 2,
  timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
  var data: T? = null
  val latch = CountDownLatch(1)
  val observer = object : androidx.lifecycle.Observer<T> {
    override fun onChanged(t: T) {
      data = t
      latch.countDown()
      this@getOrAwaitValue.removeObserver(this)
    }
  }
  this.observeForever(observer)

  // Wait for LiveData to emit
  if (!latch.await(time, timeUnit)) {
    this.removeObserver(observer)
    throw TimeoutException("LiveData value was never set.")
  }

  @Suppress("UNCHECKED_CAST")
  return data as T
}
