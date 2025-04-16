package com.example.movietracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movietracker.itemList.Item

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}