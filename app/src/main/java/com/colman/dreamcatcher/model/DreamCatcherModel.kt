package com.colman.dreamcatcher.model

import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.colman.dreamcatcher.base.DreamCatcherApplication
import com.google.firebase.firestore.DocumentSnapshot
import androidx.lifecycle.LiveData
import com.colman.dreamcatcher.model.dao.AppLocalDB
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object DreamCatcherModel {

    private val imageGenerator: DreamImageGenerator = PollinationsImageGenerator()
    private val firebaseModel = FirebaseModel()
    private val firebaseAuthModel = FirebaseAuthModel()
    private val storageModel = StorageModel()
    private val database = AppLocalDB.db

    private val sharedPrefs: SharedPreferences by lazy {
        DreamCatcherApplication.appContext!!.getSharedPreferences("LocalCache", Context.MODE_PRIVATE)
    }

    private fun getLastUpdate(): Long {
        return sharedPrefs.getLong("POSTS_LAST_UPDATE", 0L)
    }

    private fun setLastUpdate(time: Long) {
        sharedPrefs.edit { putLong("POSTS_LAST_UPDATE", time) }
    }

    fun getAllPostsLocal(): LiveData<List<DreamPost>> {
        refreshPosts()
        return database.dreamPostDao.getAllPosts()
    }

    fun getPostsByUserLocal(uid: String): LiveData<List<DreamPost>> {
        refreshPosts()
        return database.dreamPostDao.getPostsByUser(uid)
    }

    fun refreshPosts() {
        val since = getLastUpdate()
        DreamCatcherApplication.executorService.execute {
            firebaseModel.getPostsSince(since) { posts, error ->
                if (error == null && posts != null && posts.isNotEmpty()) {
                    val maxLastUpdate = posts.maxOfOrNull { it.lastUpdated } ?: since
                    database.dreamPostDao.insertPostsList(posts)
                    setLastUpdate(maxLastUpdate)
                }
            }
        }
    }

    fun generateDreamImage(prompt: String, callback: (imageUrl: String?, error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            imageGenerator.generateImage(prompt) { url, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(url, error)
                }
            }
        }
    }

    fun uploadDreamImageBytes(bytes: ByteArray, callback: (String?, String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            storageModel.uploadDreamImageBytes(StorageModel.StorageAPI.CLOUDINARY, bytes) { uri, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(uri?.toString(), error)
                }
            }
        }
    }

    fun addPost(post: DreamPost, callback: (error: String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.addPost(post) { error ->
                if (error == null) {
                    database.dreamPostDao.insertPosts(post)
                }
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
                if (success) {
                    database.dreamPostDao.insertPosts(post)
                }
                Handler(Looper.getMainLooper()).post {
                    callback(success)
                }
            }
        }
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseModel.deletePost(postId) { success ->
                if (success) {
                    database.dreamPostDao.deletePostById(postId)
                }
                Handler(Looper.getMainLooper()).post {
                    callback(success)
                }
            }
        }
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        DreamCatcherApplication.executorService.execute {
            firebaseAuthModel.signInWithEmailAndPassword(email, password) { success, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(success, error)
                }
            }
        }
    }

    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        nickname: String,
        callback: (Boolean, String?) -> Unit
    ) {
        DreamCatcherApplication.executorService.execute {
            firebaseAuthModel.createUserWithEmailAndPassword(
                email,
                password,
                nickname
            ) { success, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(success, error)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            firebaseAuthModel.signInWithGoogle(idToken) { success, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(success, error)
                }
            }
        }
    }

    fun signOut() {
        DreamCatcherApplication.executorService.execute {
            firebaseAuthModel.signOut()
        }
    }

    fun getCurrentUser() = firebaseAuthModel.getCurrentUser()

    fun updateUserProfile(
        displayName: String,
        photoUrl: Uri?,
        callback: (Boolean, String?) -> Unit
    ) {
        DreamCatcherApplication.executorService.execute {
            firebaseAuthModel.updateUserProfile(displayName, photoUrl) { success, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(success, error)
                }
            }
        }
    }

    fun uploadProfileImageBytes(bytes: ByteArray, callback: (Uri?, String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            storageModel.uploadProfileImageBytes(
                StorageModel.StorageAPI.CLOUDINARY,
                bytes
            ) { url, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(url, error)
                }
            }
        }
    }

    fun uploadProfileImage(uri: Uri, callback: (Uri?, String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            storageModel.uploadProfileImage(StorageModel.StorageAPI.CLOUDINARY, uri) { url, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(url, error)
                }
            }
        }
    }
}
