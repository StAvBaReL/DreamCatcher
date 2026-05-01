package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class EditDreamViewModel : ViewModel() {

    val post = MutableLiveData<DreamPost?>()
    val saveState = MutableLiveData(LoadingState.IDLE)
    val imageRegenState = MutableLiveData(LoadingState.IDLE)

    fun loadPost(postId: String) {
        DreamCatcherModel.getPostById(postId) { loaded ->
            post.value = loaded
        }
    }

    fun regenerateImage(prompt: String) {
        if (prompt.isBlank()) return
        val current = post.value ?: return
        
        if (prompt == current.description && current.imageUrl.isNotEmpty()) {
            imageRegenState.value = LoadingState.SUCCESS
            return
        }

        imageRegenState.value = LoadingState.LOADING
        DreamCatcherModel.generateDreamImage(prompt) { url, _ ->
            if (url != null) {
                post.value = post.value?.copy(imageUrl = url)
                imageRegenState.value = LoadingState.SUCCESS
            } else {
                imageRegenState.value = LoadingState.ERROR
            }
        }
    }

    fun savePost(title: String, description: String, imageUrl: String) {
        val current = post.value ?: return
        if (title.isBlank()) return
        val updated = current.copy(
            title = title,
            description = description,
            imageUrl = imageUrl,
            lastUpdated = System.currentTimeMillis()
        )
        saveState.value = LoadingState.LOADING
        DreamCatcherModel.updatePost(updated) { success ->
            saveState.value = if (success) LoadingState.SUCCESS else LoadingState.ERROR
        }
    }
}
