package com.colman.dreamcatcher.model

import android.os.Handler
import android.os.Looper
import com.colman.dreamcatcher.base.DreamCatcherApplication
import com.google.firebase.firestore.DocumentSnapshot

object DreamCatcherModel {

    private val imageGenerator: DreamImageGenerator = PollinationsImageGenerator()
    private val firebaseModel = FirebaseModel()

    fun generateDreamImage(prompt: String, callback: (imageUrl: String?, error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            imageGenerator.generateImage(prompt) { url, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(url, error)
                }
            }
        }
    }

    fun addPost(post: DreamPost, callback: (error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.addPost(post) { error ->
                Handler(Looper.getMainLooper()).post {
                    callback(error)
                }
            }
        }
    }

    fun getPostsPaged(
        limit: Long,
        after: DocumentSnapshot?,
        callback: (List<DreamPost>?, lastSnapshot: DocumentSnapshot?, error: String?) -> Unit
    ) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.getPostsPaged(limit, after) { posts, lastSnapshot, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(posts, lastSnapshot, error)
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

    fun getPostById(postId: String, callback: (DreamPost?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.getPostById(postId) { post ->
                Handler(Looper.getMainLooper()).post {
                    callback(post)
                }
            }
        }
    }

    fun updatePost(post: DreamPost, callback: (Boolean) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.updatePost(post) { success ->
                Handler(Looper.getMainLooper()).post {
                    callback(success)
                }
            }
        }
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.deletePost(postId) { success ->
                Handler(Looper.getMainLooper()).post {
                    callback(success)
                }
            }
        }
    }
}
