package com.colman.dreamcatcher.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
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
            .update(DreamPost.LIKES_KEY, update)
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

    fun getPostsPaged(
        limit: Long,
        after: DocumentSnapshot?,
        callback: (List<DreamPost>?, lastSnapshot: DocumentSnapshot?, error: String?) -> Unit
    ) {
        var query = db.collection(POSTS_COLLECTION)
            .orderBy(DreamPost.CREATED_AT_KEY, Query.Direction.DESCENDING)
            .limit(limit)
        if (after != null) {
            query = query.startAfter(after)
        }
        query.get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { DreamPost.fromJson(it) }
                }
                callback(posts, snapshot.documents.lastOrNull(), null)
            }
            .addOnFailureListener { e -> callback(null, null, e.message) }
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

    fun getPostsByUser(uid: String, callback: (List<DreamPost>?, error: String?) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .whereEqualTo(DreamPost.AUTHOR_UID_KEY, uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { DreamPost.fromJson(it) }
                }
                callback(posts, null)
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

    fun deletePost(postId: String, callback: (Boolean) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .document(postId)
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
