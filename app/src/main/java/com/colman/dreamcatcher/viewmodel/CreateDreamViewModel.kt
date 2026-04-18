package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost
import com.colman.dreamcatcher.base.DreamCatcherApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import android.os.Handler
import android.os.Looper

class CreateDreamViewModel : ViewModel() {

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
        
        val user = DreamCatcherModel.getCurrentUser()
        if (user == null) {
            errorMessage.value = "User not logged in"
            return
        }

        postLoadingState.value = LoadingState.LOADING
        
        DreamCatcherApplication.executorService.execute {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(imageUrl).build()
                val response = client.newCall(request).execute()
                val bytes = response.body.bytes()

                // Call directly on background thread. uploadDreamImageBytes handles thread dispatch.
                DreamCatcherModel.uploadDreamImageBytes(bytes) { secureUrl, uploadError ->
                    if (secureUrl == null) {
                        errorMessage.value = uploadError ?: "Failed to save image to cloud storage"
                        postLoadingState.value = LoadingState.ERROR
                        return@uploadDreamImageBytes
                    }

                    val now = System.currentTimeMillis()
                    val post = DreamPost(
                        postId = UUID.randomUUID().toString(),
                        authorUid = user.uid,
                        authorNickname = user.displayName ?: "Dreamer",
                        authorProfilePicUrl = user.photoUrl?.toString(),
                        title = title,
                        description = description,
                        imageUrl = secureUrl,
                        createdAt = now,
                        lastUpdated = now
                    )
                    
                    DreamCatcherModel.addPost(post) { error ->
                        if (error == null) {
                            postLoadingState.value = LoadingState.SUCCESS
                        } else {
                            errorMessage.value = error
                            postLoadingState.value = LoadingState.ERROR
                        }
                    }
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    errorMessage.value = "Network error while saving post: ${e.message}"
                    postLoadingState.value = LoadingState.ERROR
                }
            }
        }
    }
}
