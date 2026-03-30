package com.colman.dreamcatcher.model

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseModel {

    private val db = Firebase.firestore

    companion object {
        const val POSTS_COLLECTION = "posts"
    }

    fun addPost(post: DreamPost, callback: (error: String?) -> Unit) {
        db.collection(POSTS_COLLECTION)
            .document(post.postId)
            .set(post.toJson)
            .addOnSuccessListener { callback(null) }
            .addOnFailureListener { e -> callback(e.message) }
    }

    fun getAllPosts(since: Long, callback: (List<DreamPost>?, error: String?) -> Unit) {
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
