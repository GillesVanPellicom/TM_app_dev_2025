package com.example.movietracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedItemDao {
  // Trending cache
  @Query("SELECT * FROM CachedItems WHERE page = :page AND searchQuery IS NULL ORDER BY popularity DESC")
  suspend fun getTrendingPage(page: Int): List<CachedItem>

  @Query("DELETE FROM CachedItems WHERE page = :page AND searchQuery IS NULL")
  suspend fun clearTrendingPage(page: Int)

  // Search cache
  @Query("SELECT * FROM CachedItems WHERE searchQuery = :searchQuery ORDER BY popularity DESC")
  suspend fun getSearchResults(searchQuery: String): List<CachedItem>

  @Query("DELETE FROM CachedItems WHERE searchQuery = :searchQuery")
  suspend fun clearSearchResults(searchQuery: String)

  // Inserts
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<CachedItem>)

  // Cleanup (optional TTL-based purge left to repository logic)
  @Query("DELETE FROM CachedItems WHERE cachedAt < :olderThan")
  suspend fun deleteOlderThan(olderThan: Long)
}