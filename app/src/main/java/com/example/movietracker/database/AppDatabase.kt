package com.example.movietracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movietracker.itemList.Item

@Database(entities = [Item::class, CachedItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun itemDao(): ItemDao
  abstract fun cachedItemDao(): CachedItemDao
}