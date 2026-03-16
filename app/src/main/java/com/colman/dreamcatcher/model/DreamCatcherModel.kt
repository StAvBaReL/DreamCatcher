package com.colman.dreamcatcher.model

import android.os.Handler
import android.os.Looper
import com.colman.dreamcatcher.base.DreamCatcherApplication

object DreamCatcherModel {

    private val imageGenerator: DreamImageGenerator = PollinationsImageGenerator()

    fun generateDreamImage(prompt: String, callback: (imageUrl: String?, error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            imageGenerator.generateImage(prompt) { url, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(url, error)
                }
            }
        }
    }
}
