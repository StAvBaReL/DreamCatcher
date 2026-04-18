package com.colman.dreamcatcher.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DreamCatcherApplication : Application() {

    companion object Globals {
        var appContext: Context? = null
        val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
