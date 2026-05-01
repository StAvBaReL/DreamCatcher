package com.colman.dreamcatcher.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings

class FirebaseModel {

    private val db = Firebase.firestore

    init {
        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {})
        }
        db.firestoreSettings = settings
    }

    companion object {
        const val POSTS_COLLECTION = "posts"
    }

    fun toggleLike(postId: String, uid: String, wasLiked: Boolean, callback: (Boolean) -> Unit) {
        val update = if (wasLiked) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid)
        db.collection(POSTS_COLLECTION).document(postId)
            .update(
                DreamPost.LIKES_KEY, update,
                DreamPost.LAST_UPDATED_KEY, System.currentTimeMillis()
            )
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun addPost(post: DreamPost, callback: (error: String?) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .document(post.postId)
            .set(post.toJson)
            .addOnSuccessListener { callback(null) }
            .addOnFailureListener { e -> callback(e.message) }
    }

    fun getPostsSince(since: Long, callback: (List<DreamPost>?, error: String?) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .whereGreaterThan(DreamPost.LAST_UPDATED_KEY, since)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { DreamPost.fromJson(it) }
                }
                callback(posts, null)
            }
            .addOnFailureListener { e -> callback(null, e.message) }
    }

    fun getAllActivePostIds(callback: (List<String>?, error: String?) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .whereEqualTo(DreamPost.IS_DELETED_KEY, false)
            .get(Source.SERVER)
            .addOnSuccessListener { snapshot ->
                val ids = snapshot.documents.map { it.id }
                callback(ids, null)
            }
            .addOnFailureListener { e -> callback(null, e.message) }
    }

    fun getPostById(postId: String, callback: (DreamPost?) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .document(postId)
            .get()
            .addOnSuccessListener { doc ->
                val post = doc.data?.let { DreamPost.fromJson(it) }
                callback(post)
            }
            .addOnFailureListener { callback(null) }
    }

    fun updatePost(post: DreamPost, callback: (Boolean) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .document(post.postId)
            .set(post.toJson)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun updateUserPostsAuthorDetails(
        uid: String,
        nickname: String,
        photoUrl: String?,
        callback: (Boolean) -> Unit
    ) {
        db.collection(POSTS_COLLECTION)
            .whereEqualTo(DreamPost.AUTHOR_UID_KEY, uid)
            .get()
            .addOnSuccessListener { snapshot ->
                db.runBatch { batch ->
                    val now = System.currentTimeMillis()
                    for (doc in snapshot.documents) {
                        batch.update(
                            doc.reference,
                            DreamPost.AUTHOR_NICKNAME_KEY, nickname,
                            DreamPost.AUTHOR_PROFILE_PIC_URL_KEY, photoUrl,
                            DreamPost.LAST_UPDATED_KEY, now
                        )
                    }
                }.addOnSuccessListener {
                    callback(true)
                }.addOnFailureListener {
                    callback(false)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .document(postId)
            .update(
                DreamPost.IS_DELETED_KEY, true,
                DreamPost.LAST_UPDATED_KEY, System.currentTimeMillis()
            )
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
