package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel

class AuthViewModel : ViewModel() {
    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> = _loginState

    private val _registerState = MutableLiveData<Boolean>()
    val registerState: LiveData<Boolean> = _registerState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun isUserLoggedIn() = DreamCatcherModel.getCurrentUser() != null

    fun login(email: String, password: String) {
        _isLoading.value = true
        DreamCatcherModel.signInWithEmailAndPassword(email, password) { success, error ->
            _isLoading.value = false
            if (success) {
                _loginState.value = true
            } else {
                _errorMessage.value = error ?: "Login failed"
            }
        }
    }

    fun register(email: String, password: String, nickname: String) {
        _isLoading.value = true
        DreamCatcherModel.createUserWithEmailAndPassword(
            email,
            password,
            nickname
        ) { success, error ->
            _isLoading.value = false
            if (success) {
                _registerState.value = true
            } else {
                _errorMessage.value = error ?: "Registration failed"
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _isLoading.value = true
        DreamCatcherModel.signInWithGoogle(idToken) { success, error ->
            _isLoading.value = false
            if (success) {
                _loginState.value = true
            } else {
                _errorMessage.value = error ?: "Google sign in failed"
            }
        }
    }

    fun signOut() {
        DreamCatcherModel.signOut()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
