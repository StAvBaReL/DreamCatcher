package com.colman.dreamcatcher.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
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

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .skipMemoryCache(true)

        Glide.init(
            this,
            GlideBuilder().setDefaultRequestOptions(requestOptions)
        )
    }
}
