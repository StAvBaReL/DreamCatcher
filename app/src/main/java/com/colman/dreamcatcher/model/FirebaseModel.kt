package com.colman.dreamcatcher.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.ktx.Firebase

class FirebaseModel {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

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

    fun signInWithEmailAndPassword(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, nickname: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            callback(true, null)
                        } ?: callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun signInWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = auth.currentUser
}
