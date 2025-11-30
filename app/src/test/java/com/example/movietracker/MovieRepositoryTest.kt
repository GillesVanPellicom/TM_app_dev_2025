package com.example.movietracker

import com.example.movietracker.api.TmdbService
import com.example.movietracker.api.TrendingListItem
import com.example.movietracker.api.TrendingResponse
import com.example.movietracker.database.CachedItem
import com.example.movietracker.database.CachedItemDao
import com.example.movietracker.itemList.Item
import com.example.movietracker.repository.MovieRepository
import com.example.movietracker.repository.OfflineNoCacheException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MovieRepositoryTest {

  @Test
  fun getTrending_networkSuccess_cachesAndReturnsItems() = runBlocking {
    val fakeDao = InMemoryCachedItemDao()
    val fakeService = FakeTmdbService(shouldFail = false)
    val repo = MovieRepository(fakeDao, fakeService, apiKey = "test")

    val result = repo.getTrending(page = 1)

    // Verify network items returned
    assertEquals(2, result.size)
    assertEquals(listOf(100, 200), result.map { it.tmbdId })

    // Verify cached
    val cached = fakeDao.getTrendingPage(1)
    assertEquals(2, cached.size)
  }

  @Test
  fun getTrending_networkFails_usesCacheOrThrows() = runBlocking {
    val fakeDao = InMemoryCachedItemDao()
    val failingService = FakeTmdbService(shouldFail = true)
    val repo = MovieRepository(fakeDao, failingService, apiKey = "test")

    // No cache yet -> expect exception
    assertThrows(OfflineNoCacheException::class.java) {
      runBlocking {
        repo.getTrending(page = 1)
      }
    }

    // Seed cache, then expect fallback to cached values when network fails
    fakeDao.insertAll(
      listOf(
        CachedItem(
          tmbdId = 123, imageUrl = "", title = "Cached", subTitle = "", isFilm = true,
          page = 1, searchQuery = null, popularity = 1.0
        )
      )
    )

    val cachedResult = repo.getTrending(page = 1, ttlMs = 0) // force stale path -> fallback
    assertEquals(1, cachedResult.size)
    assertEquals(123, cachedResult.first().tmbdId)
  }

  // ----------------- Fakes -----------------
  private class InMemoryCachedItemDao : CachedItemDao {
    private val data = mutableListOf<CachedItem>()

    override suspend fun getTrendingPage(page: Int): List<CachedItem> =
      data.filter { it.page == page && it.searchQuery == null }

    override suspend fun clearTrendingPage(page: Int) {
      data.removeAll { it.page == page && it.searchQuery == null }
    }

    override suspend fun getSearchResults(searchQuery: String): List<CachedItem> =
      data.filter { it.searchQuery == searchQuery }

    override suspend fun clearSearchResults(searchQuery: String) {
      data.removeAll { it.searchQuery == searchQuery }
    }

    override suspend fun insertAll(items: List<CachedItem>) {
      data.addAll(items)
    }

    override suspend fun deleteOlderThan(olderThan: Long) {
      data.removeAll { it.cachedAt < olderThan }
    }
  }

  private class FakeTmdbService(private val shouldFail: Boolean) : TmdbService {
    override fun getTrending(apiKey: String, page: Int): Call<TrendingResponse> {
      return object : Call<TrendingResponse> {
        override fun enqueue(callback: Callback<TrendingResponse>) {
          if (shouldFail) {
            callback.onFailure(this, RuntimeException("network"))
          } else {
            val body = TrendingResponse(
              listOf(
                TrendingListItem(
                  id = 100,
                  title = "Title A",
                  name = null,
                  overview = null,
                  posterPath = "/a.jpg",
                  movieReleaseDate = "2024-01-01",
                  tvReleaseDate = null,
                  mediaType = "movie",
                  popularity = 2.0
                ),
                TrendingListItem(
                  id = 200,
                  title = null,
                  name = "Name B",
                  overview = null,
                  posterPath = "/b.jpg",
                  movieReleaseDate = null,
                  tvReleaseDate = "2023-02-02",
                  mediaType = "tv",
                  popularity = 1.0
                )
              )
            )
            callback.onResponse(this, Response.success(body))
          }
        }

        // Unused in tests
        override fun isExecuted() = false
        override fun clone(): Call<TrendingResponse> = this
        override fun isCanceled() = false
        override fun cancel() {}
        override fun execute(): Response<TrendingResponse> = Response.success(null)
        override fun request() = okhttp3.Request.Builder().url("https://example.com").build()
        override fun timeout(): okio.Timeout = okio.Timeout.NONE
      }
    }

    // Unused endpoints for these tests
    override fun getMovie(movieId: Int, apiKey: String) = throw NotImplementedError()
    override fun getTvShow(id: Int, apiKey: String) = throw NotImplementedError()
    override fun searchMoviesOrShows(apiKey: String, query: String, page: Int, language: String): Call<TrendingResponse> =
      throw NotImplementedError()
  }
}
