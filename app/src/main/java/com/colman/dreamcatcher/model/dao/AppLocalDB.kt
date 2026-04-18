package com.colman.dreamcatcher.model.dao

import androidx.room.Room
import com.colman.dreamcatcher.base.DreamCatcherApplication

object AppLocalDB {
    val db: AppLocalDbRepository by lazy {
        val context = DreamCatcherApplication.appContext
            ?: throw IllegalStateException("Context is null")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dreamcatcher.db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}

