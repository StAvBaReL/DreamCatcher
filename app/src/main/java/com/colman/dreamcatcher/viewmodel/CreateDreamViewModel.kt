package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel

class CreateDreamViewModel : ViewModel() {

    val loadingState = MutableLiveData(LoadingState.IDLE)
    val generatedImageUrl = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

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
}
