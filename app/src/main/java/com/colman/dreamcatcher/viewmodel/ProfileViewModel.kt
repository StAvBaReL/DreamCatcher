package com.colman.dreamcatcher.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class ProfileViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateState = MutableLiveData<Boolean>()
    val updateState: LiveData<Boolean> = _updateState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val userDreams: LiveData<PagingData<DreamPost>> by lazy {
        val uid = DreamCatcherModel.getCurrentUser()?.uid ?: ""
        DreamCatcherModel.getPostsByUserLocal(uid).cachedIn(viewModelScope)
    }

    private val userDreamsRaw: LiveData<List<DreamPost>> by lazy {
        val uid = DreamCatcherModel.getCurrentUser()?.uid ?: ""
        DreamCatcherModel.getPostsByUserRaw(uid)
    }

    val dreamsCount: LiveData<Int> = userDreamsRaw.map { it.size }

    val likesCount: LiveData<Int> = userDreamsRaw.map { posts ->
        posts.sumOf { it.likes.size }
    }

    fun getCurrentUser() = DreamCatcherModel.getCurrentUser()

    fun fetchUserStats() {
        DreamCatcherModel.refreshPosts()
    }

    private fun handleProfileUpdateSuccess(displayName: String, photoUriStr: String?) {
        val uid = getCurrentUser()?.uid ?: ""
        if (uid.isNotEmpty()) {
            DreamCatcherModel.updateUserPostsAuthorDetails(uid, displayName, photoUriStr) {}
        }
        _updateState.value = true
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
                            handleProfileUpdateSuccess(displayName, urlUri.toString())
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
            DreamCatcherModel.uploadProfileImage(newPhotoUri) { urlUri, error ->
                if (urlUri != null) {
                    DreamCatcherModel.updateUserProfile(
                        displayName,
                        urlUri
                    ) { success, updateError ->
                        _isLoading.value = false
                        if (success) {
                            handleProfileUpdateSuccess(displayName, urlUri.toString())
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
            val uriToSave =
                if (newPhotoUri != null && (newPhotoUri.scheme == "https" || newPhotoUri.scheme == "http")) {
                    newPhotoUri
                } else {
                    getCurrentUser()?.photoUrl
                }

            DreamCatcherModel.updateUserProfile(displayName, uriToSave) { success, updateError ->
                _isLoading.value = false
                if (success) {
                    handleProfileUpdateSuccess(displayName, uriToSave?.toString())
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
