package com.colman.dreamcatcher.base

import android.app.Application
import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors.newFixedThreadPool

class DreamCatcherApplication : Application() {

    companion object Globals {
        var appContext: Context? = null
        val executorService: ExecutorService = newFixedThreadPool(4)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
