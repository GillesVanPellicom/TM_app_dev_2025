package com.example.movietracker.repository

import com.example.movietracker.api.TmdbService
import com.example.movietracker.api.TrendingResponse
import com.example.movietracker.database.CachedItem
import com.example.movietracker.database.CachedItemDao
import com.example.movietracker.itemList.Item
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MovieRepository(
  private val cachedItemDao: CachedItemDao,
  private val service: TmdbService,
  private val apiKey: String
) {
  companion object {
    const val ONE_HOUR_MS: Long = 60 * 60 * 1000L
    const val FIFTEEN_MIN_MS: Long = 15 * 60 * 1000L
  }

  suspend fun getTrending(page: Int, ttlMs: Long = ONE_HOUR_MS): List<Item> {
    // Try cache first
    val cached = cachedItemDao.getTrendingPage(page)
    val now = System.currentTimeMillis()
    val freshestTs = cached.maxOfOrNull { it.cachedAt } ?: 0L

    if (cached.isNotEmpty() && now - freshestTs <= ttlMs) {
      // Fresh cache hit
      return cached.map { it.toItem() }
    }

    // Cache is empty or stale -> try network
    val networkItems = fetchTrendingFromNetwork(page)
    return if (networkItems != null) {
      // Replace page in cache
      cachedItemDao.clearTrendingPage(page)
      cachedItemDao.insertAll(networkItems.map { it.toCached(page = page, query = null) })
      networkItems
    } else {
      // Network failed
      if (cached.isNotEmpty()) {
        // Fallback to any cached data available (even if stale)
        cached.map { it.toItem() }
      } else {
        // No cache available: surface an error so UI can show reload dialog/state
        throw OfflineNoCacheException("No internet and no cached trending page $page available")
      }
    }
  }

  suspend fun search(query: String, page: Int = 1, ttlMs: Long = FIFTEEN_MIN_MS): List<Item> {
    val cached = cachedItemDao.getSearchResults(query)
    val now = System.currentTimeMillis()
    val freshestTs = cached.maxOfOrNull { it.cachedAt } ?: 0L

    if (cached.isNotEmpty() && now - freshestTs <= ttlMs) {
      return cached.map { it.toItem() }
    }

    val networkItems = fetchSearchFromNetwork(query, page)
    return if (networkItems != null) {
      cachedItemDao.clearSearchResults(query)
      cachedItemDao.insertAll(networkItems.map { it.toCached(page = null, query = query) })
      networkItems
    } else {
      if (cached.isNotEmpty()) {
        cached.map { it.toItem() }
      } else {
        throw OfflineNoCacheException("No internet and no cached search results for query '$query'")
      }
    }
  }

  private suspend fun fetchTrendingFromNetwork(page: Int): List<Item>? = suspendCoroutine { cont ->
    service.getTrending(apiKey, page).enqueue(object : Callback<TrendingResponse> {
      override fun onResponse(call: Call<TrendingResponse>, response: Response<TrendingResponse>) {
        if (response.isSuccessful) {
          val items = response.body()?.results?.map {
            Item(
              tmbdId = it.id,
              title = it.title ?: it.name.orEmpty(),
              subTitle = it.formattedReleaseInfo ?: "Release info unavailable",
              imageUrl = "https://image.tmdb.org/t/p/w500${it.posterPath}",
              isFilm = it.mediaType == "movie"
            )
          } ?: emptyList()
          cont.resume(items)
        } else {
          cont.resume(null)
        }
      }

      override fun onFailure(call: Call<TrendingResponse>, t: Throwable) {
        cont.resume(null)
      }
    })
  }

  private suspend fun fetchSearchFromNetwork(query: String, page: Int): List<Item>? = suspendCoroutine { cont ->
    service.searchMoviesOrShows(apiKey, query, page, "en-US").enqueue(object : Callback<TrendingResponse> {
      override fun onResponse(call: Call<TrendingResponse>, response: Response<TrendingResponse>) {
        if (response.isSuccessful) {
          val items = response.body()?.results
            ?.sortedByDescending { it.popularity }
            ?.map {
              Item(
                tmbdId = it.id,
                title = it.title ?: it.name.orEmpty(),
                subTitle = it.formattedReleaseInfo ?: "Release info unavailable",
                imageUrl = "https://image.tmdb.org/t/p/w500${it.posterPath}",
                isFilm = it.mediaType == "movie"
              )
            } ?: emptyList()
          cont.resume(items)
        } else {
          cont.resume(null)
        }
      }

      override fun onFailure(call: Call<TrendingResponse>, t: Throwable) {
        cont.resume(null)
      }
    })
  }
}

private fun CachedItem.toItem(): Item = Item(
  tmbdId = tmbdId,
  imageUrl = imageUrl,
  title = title,
  subTitle = subTitle,
  isFilm = isFilm
)

private fun Item.toCached(page: Int?, query: String?): CachedItem = CachedItem(
  tmbdId = tmbdId,
  imageUrl = imageUrl,
  title = title,
  subTitle = subTitle,
  isFilm = isFilm,
  page = page,
  searchQuery = query,
  popularity = 0.0 // Unknown at this stage; we keep API order
)