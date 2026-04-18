package com.colman.dreamcatcher.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

inline fun requireUser(
    crossinline onError: (String) -> Unit,
    block: (FirebaseUser) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
        block(user)
    } else {
        onError("No user logged in")
    }
}

