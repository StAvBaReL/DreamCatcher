package com.colman.dreamcatcher.model.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colman.dreamcatcher.model.DreamPost

@Database(entities = [DreamPost::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract val dreamPostDao: DreamPostDao
}
