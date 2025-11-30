package com.example.movietracker

import com.example.movietracker.api.TrendingListItem
import org.junit.Assert.assertEquals
import org.junit.Test

class TrendingListItemTest {

  @Test
  fun formattedReleaseInfo_movie_usesMovieYear() {
    val item = TrendingListItem(
      id = 1,
      title = "Movie Title",
      name = null,
      overview = null,
      posterPath = null,
      movieReleaseDate = "2024-08-15",
      tvReleaseDate = null,
      mediaType = "movie",
      popularity = 10.0
    )

    assertEquals("2024", item.formattedReleaseInfo)
    assertEquals("2024-08-15", item.releaseDate)
  }

  @Test
  fun formattedReleaseInfo_tv_usesTvYear() {
    val item = TrendingListItem(
      id = 2,
      title = null,
      name = "TV Name",
      overview = null,
      posterPath = null,
      movieReleaseDate = null,
      tvReleaseDate = "2020-01-02",
      mediaType = "tv",
      popularity = 7.0
    )

    assertEquals("2020", item.formattedReleaseInfo)
    assertEquals("2020-01-02", item.releaseDate)
  }
}
