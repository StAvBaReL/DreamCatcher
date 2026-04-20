package com.colman.dreamcatcher.model

import android.content.Context
import androidx.core.content.edit
import com.colman.dreamcatcher.base.DreamCatcherApplication

object LocalSyncManager {

    private const val PREFS_NAME = "dreamcatcher_sync_prefs"
    private const val LAST_UPDATED_KEY = "last_updated"

    private val prefs = DreamCatcherApplication.appContext?.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getLastSyncTimestamp(): Long {
        return prefs?.getLong(LAST_UPDATED_KEY, 0L) ?: 0L
    }

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs?.edit {
            putLong(LAST_UPDATED_KEY, timestamp)
        }
    }
}


