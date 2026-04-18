package com.colman.dreamcatcher.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel

class ProfileViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateState = MutableLiveData<Boolean>()
    val updateState: LiveData<Boolean> = _updateState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _dreamsCount = MutableLiveData(0)
    val dreamsCount: LiveData<Int> = _dreamsCount

    private val _likesCount = MutableLiveData(0)
    val likesCount: LiveData<Int> = _likesCount

    private val _userDreams = MutableLiveData<List<com.colman.dreamcatcher.model.DreamPost>>()
    val userDreams: LiveData<List<com.colman.dreamcatcher.model.DreamPost>> = _userDreams

    fun getCurrentUser() = DreamCatcherModel.getCurrentUser()

    fun fetchUserStats() {
        val uid = getCurrentUser()?.uid ?: return
        DreamCatcherModel.getPostsByUser(uid) { posts, _ ->
            if (posts != null) {
                _userDreams.value = posts
                _dreamsCount.value = posts.size

                var totalLikes = 0
                posts.forEach { post ->
                    totalLikes += post.likes.size
                }
                _likesCount.value = totalLikes
            } else {
                _userDreams.value = emptyList()
                _dreamsCount.value = 0
                _likesCount.value = 0
            }
        }
    }

    fun updateProfile(displayName: String, newPhotoUri: Uri?, newPhotoBytes: ByteArray?) {
        _isLoading.value = true
        if (newPhotoBytes != null) {
            DreamCatcherModel.uploadProfileImageBytes(newPhotoBytes) { urlUri, error ->
                if (urlUri != null) {
                    DreamCatcherModel.updateUserProfile(
                        displayName,
                        urlUri
                    ) { success, updateError ->
                        _isLoading.value = false
                        if (success) {
                            _updateState.value = true
                        } else {
                            _errorMessage.value = updateError ?: "Failed to update profile"
                        }
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = error ?: "Failed to upload image"
                }
            }
        } else if (newPhotoUri != null && newPhotoUri.scheme != "https" && newPhotoUri.scheme != "http") {
            // Needs uploading
            DreamCatcherModel.uploadProfileImage(newPhotoUri) { urlUri, error ->
                if (urlUri != null) {
                    DreamCatcherModel.updateUserProfile(
                        displayName,
                        urlUri
                    ) { success, updateError ->
                        _isLoading.value = false
                        if (success) {
                            _updateState.value = true
                        } else {
                            _errorMessage.value = updateError ?: "Failed to update profile"
                        }
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = error ?: "Failed to upload image"
                }
            }
        } else {
            // Just update display name, keep existing photo (or set new photo string)
            val uriToSave =
                if (newPhotoUri != null && (newPhotoUri.scheme == "https" || newPhotoUri.scheme == "http")) {
                    newPhotoUri
                } else {
                    getCurrentUser()?.photoUrl
                }

            DreamCatcherModel.updateUserProfile(displayName, uriToSave) { success, updateError ->
                _isLoading.value = false
                if (success) {
                    _updateState.value = true
                } else {
                    _errorMessage.value = updateError ?: "Failed to update profile"
                }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
