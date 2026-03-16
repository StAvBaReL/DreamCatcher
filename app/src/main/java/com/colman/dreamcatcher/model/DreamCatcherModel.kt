package com.colman.dreamcatcher.model

import android.os.Handler
import android.os.Looper
import com.colman.dreamcatcher.base.DreamCatcherApplication

object DreamCatcherModel {

    private val firebaseModel = FirebaseModel()

    fun addPost(post: DreamPost, callback: (error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.addPost(post) { error ->
                Handler(Looper.getMainLooper()).post {
                    callback(error)
                }
            }
        }
    }

    fun getAllPosts(since: Long, callback: (List<DreamPost>?, error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.getAllPosts(since) { posts, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(posts, error)
                }
            }
        }
    }

    fun getPostsByUser(uid: String, callback: (List<DreamPost>?, error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.getPostsByUser(uid) { posts, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(posts, error)
                }
            }
        }
    }
}
