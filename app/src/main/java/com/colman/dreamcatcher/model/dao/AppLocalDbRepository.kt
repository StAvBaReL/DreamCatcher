package com.colman.dreamcatcher.model.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colman.dreamcatcher.model.DreamPost

@Database(entities = [DreamPost::class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract val dreamPostDao: DreamPostDao
}
