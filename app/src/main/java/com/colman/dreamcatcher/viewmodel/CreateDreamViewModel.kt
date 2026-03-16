package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost
import java.util.UUID

class CreateDreamViewModel : ViewModel() {

    companion object {
        const val MOCK_UID = "mock_user_001"
        const val MOCK_NICKNAME = "Dreamer"
    }

    val loadingState = MutableLiveData(LoadingState.IDLE)
    val generatedImageUrl = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()
    val postLoadingState = MutableLiveData(LoadingState.IDLE)

    fun visualizeDream(prompt: String) {
        if (prompt.isBlank()) {
            errorMessage.value = "Please describe your dream first"
            return
        }
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.generateDreamImage(prompt) { url, error ->
            if (url != null) {
                generatedImageUrl.value = url
                loadingState.value = LoadingState.SUCCESS
            } else {
                errorMessage.value = error ?: "Failed to generate image"
                loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun postDream(title: String, description: String, imageUrl: String) {
        if (title.isBlank()) {
            errorMessage.value = "Please give your dream a title"
            return
        }
        val now = System.currentTimeMillis()
        val post = DreamPost(
            postId = UUID.randomUUID().toString(),
            authorUid = MOCK_UID,
            authorNickname = MOCK_NICKNAME,
            authorProfilePicUrl = null,
            title = title,
            description = description,
            imageUrl = imageUrl,
            createdAt = now,
            lastUpdated = now
        )
        postLoadingState.value = LoadingState.LOADING
        DreamCatcherModel.addPost(post) { error ->
            if (error == null) {
                postLoadingState.value = LoadingState.SUCCESS
            } else {
                errorMessage.value = error
                postLoadingState.value = LoadingState.ERROR
            }
        }
    }
}
