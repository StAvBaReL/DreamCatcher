package com.colman.dreamcatcher.model

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.colman.dreamcatcher.base.DreamCatcherApplication
import com.colman.dreamcatcher.model.dao.AppLocalDB

object DreamCatcherModel {

    private val imageGenerator: DreamImageGenerator = PollinationsImageGenerator()
    private val firebaseModel = FirebaseModel()
    private val firebaseAuthModel = FirebaseAuthModel()
    private val storageModel = StorageModel()
    private val database = AppLocalDB.db

    val allPosts: LiveData<PagingData<DreamPost>> by lazy {
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { database.dreamPostDao.getAllPostsPaged() }
        ).liveData
    }

    fun getPostsByUserLocal(uid: String): LiveData<PagingData<DreamPost>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { database.dreamPostDao.getPostsByUserPaged(uid) }
        ).liveData
    }

    fun getPostsByUserRaw(uid: String): LiveData<List<DreamPost>> {
        return database.dreamPostDao.getPostsByUser(uid)
    }

    fun refreshPosts() {
        val lastUpdate = LocalSyncManager.getLastSyncTimestamp()
        firebaseModel.getPostsSince(lastUpdate) { posts, error ->
            if (error == null && posts != null) {
                DreamCatcherApplication.executorService.execute {
                    var latestUpdate = lastUpdate

                    for (post in posts) {
                        database.dreamPostDao.insertPosts(post)
                        if (post.lastUpdated > latestUpdate) {
                            latestUpdate = post.lastUpdated
                        }
                    }
                    LocalSyncManager.setLastSyncTimestamp(latestUpdate)

                    firebaseModel.getAllActivePostIds { remoteIds, idError ->
                        if (idError == null && remoteIds != null) {
                            DreamCatcherApplication.executorService.execute {
                                val localIds = database.dreamPostDao.getAllPostIds()
                                val deletedIds = localIds - remoteIds.toSet()

                                for (deletedId in deletedIds) {
                                    database.dreamPostDao.deletePostById(deletedId)
                                }
                            }
                        }
                    }
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
            storageModel.uploadDreamImageBytes(
                StorageModel.StorageAPI.CLOUDINARY, bytes
            ) { uri, error ->
                Handler(Looper.getMainLooper()).post {
                    callback(uri?.toString(), error)
                }
            }
        }
    }

    fun toggleLike(updatedPost: DreamPost, uid: String, wasLiked: Boolean, callback: (Boolean) -> Unit) {
        firebaseModel.toggleLike(updatedPost.postId, uid, wasLiked) { success ->
            if (success) {
                DreamCatcherApplication.executorService.execute {
                    database.dreamPostDao.insertPosts(updatedPost)
                }
            }
            Handler(Looper.getMainLooper()).post { callback(success) }
        }
    }

    fun addPost(post: DreamPost, callback: (error: String?) -> Unit) {
        firebaseModel.addPost(post) { error ->
            DreamCatcherApplication.executorService.execute {
                if (error == null) {
                    database.dreamPostDao.insertPosts(post)
                }
            }
            callback(error)
        }
    }

    fun getPostById(postId: String, callback: (DreamPost?) -> Unit) {
        firebaseModel.getPostById(postId) { post ->
            Handler(Looper.getMainLooper()).post {
                callback(post)
            }
        }
    }

    fun updatePost(post: DreamPost, callback: (Boolean) -> Unit) {
        firebaseModel.updatePost(post) { success ->
            DreamCatcherApplication.executorService.execute {
                if (success) {
                    database.dreamPostDao.insertPosts(post)
                }
            }
            Handler(Looper.getMainLooper()).post { callback(success) }
        }
    }

    fun updateUserPostsAuthorDetails(
        uid: String, nickname: String, photoUrl: String?, callback: (Boolean) -> Unit
    ) {
        firebaseModel.updateUserPostsAuthorDetails(uid, nickname, photoUrl) { success ->
            if (success) {
                refreshPosts()
            }
            Handler(Looper.getMainLooper()).post {
                callback(success)
            }
        }
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        firebaseModel.deletePost(postId) { success ->
            DreamCatcherApplication.executorService.execute {
                if (success) {
                    database.dreamPostDao.deletePostById(postId)
                }
            }
            Handler(Looper.getMainLooper()).post { callback(success) }
        }
    }

    fun signInWithEmailAndPassword(
        email: String, password: String, callback: (Boolean, String?) -> Unit
    ) {
        firebaseAuthModel.signInWithEmailAndPassword(email, password, callback)
    }

    fun createUserWithEmailAndPassword(
        email: String, password: String, nickname: String, callback: (Boolean, String?) -> Unit
    ) {
        firebaseAuthModel.createUserWithEmailAndPassword(email, password, nickname, callback)
    }

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuthModel.signInWithGoogle(idToken, callback)
    }

    fun signOut() {
        firebaseAuthModel.signOut()
    }

    fun getCurrentUser() = firebaseAuthModel.getCurrentUser()

    fun updateUserProfile(
        displayName: String, photoUrl: Uri?, callback: (Boolean, String?) -> Unit
    ) {
        firebaseAuthModel.updateUserProfile(displayName, photoUrl, callback)
    }

    fun syncCurrentUserProfileToPosts(
        displayName: String,
        photoUrl: String?,
        callback: (Boolean) -> Unit = {}
    ) {
        val user = getCurrentUser()

        if (user == null) {
            Handler(Looper.getMainLooper()).post { callback(false) }
            return
        }

        updateUserPostsAuthorDetails(user.uid, displayName, photoUrl, callback)
    }

    fun uploadProfileImageBytes(bytes: ByteArray, callback: (Uri?, String?) -> Unit) {
        DreamCatcherApplication.executorService.execute {
            storageModel.uploadProfileImageBytes(
                StorageModel.StorageAPI.CLOUDINARY, bytes
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
