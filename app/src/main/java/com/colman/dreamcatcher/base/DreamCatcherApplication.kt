package com.colman.dreamcatcher.base

import android.app.Application
import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DreamCatcherApplication : Application() {

    companion object {
        var appContext: Context? = null
        val executorService: ExecutorService = Executors.newFixedThreadPool(4)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
