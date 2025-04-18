package com.example.movietracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.movietracker.itemList.Item

@Dao
interface ItemDao {
  @Insert
  suspend fun insert(item: Item)

  @Query("SELECT * FROM UserItems ORDER BY creationdate DESC")
  suspend fun getAllItems(): List<Item>

  @Query("SELECT * FROM UserItems WHERE isFilm = :isFilm")
  suspend fun getItemsByType(isFilm: Boolean): List<Item>

  @Delete
  suspend fun delete(item: Item)
}