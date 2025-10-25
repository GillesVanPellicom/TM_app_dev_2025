package com.example.movietracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CachedItems")
data class CachedItem(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val tmbdId: Int,
  val imageUrl: String,
  val title: String,
  val subTitle: String,
  val isFilm: Boolean,
  val page: Int?,            // For trending pages; null for search
  val searchQuery: String?,        // For search results; null for trending
  val popularity: Double = 0.0,
  val cachedAt: Long = System.currentTimeMillis()
)