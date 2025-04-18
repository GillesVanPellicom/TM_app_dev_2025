package com.example.movietracker.itemList

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserItems")
data class Item(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val tmbdId: Int,
  val imageUrl: String,
  val title: String,
  val subTitle: String,
  val creationdate: Long = System.currentTimeMillis(),
  val isFilm: Boolean
)