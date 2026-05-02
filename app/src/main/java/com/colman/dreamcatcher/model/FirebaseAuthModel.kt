package com.colman.dreamcatcher.model

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class FirebaseAuthModel {

    private val auth = FirebaseAuth.getInstance()

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        nickname: String,
        callback: (Boolean, String?) -> Unit
    ) {
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

    fun updateUserProfile(
        displayName: String,
        photoUrl: Uri?,
        callback: (Boolean, String?) -> Unit
    ) {
        requireUser({ error -> callback(false, error) }) { user ->
            val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)

            if (photoUrl != null) {
                profileUpdatesBuilder.photoUri = photoUrl
            }

            user.updateProfile(profileUpdatesBuilder.build())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, task.exception?.message)
                    }
                }
        }
    }
}
