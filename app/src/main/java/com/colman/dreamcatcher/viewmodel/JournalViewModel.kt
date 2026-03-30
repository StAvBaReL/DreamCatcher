package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class JournalViewModel : ViewModel() {

    companion object {
        const val MOCK_UID = "mock_user_001"
    }

    val posts = MutableLiveData<List<DreamPost>>(emptyList())
    val loadingState = MutableLiveData(LoadingState.IDLE)

    fun loadPosts() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.getPostsByUser(MOCK_UID) { list, error ->
            if (error == null) {
                posts.value = list ?: emptyList()
                loadingState.value = LoadingState.SUCCESS
            } else {
                loadingState.value = LoadingState.ERROR
            }
        }
    }

    fun deletePost(postId: String, onDone: () -> Unit) {
        DreamCatcherModel.deletePost(postId) { success ->
            if (success) {
                loadPosts()
            }
            onDone()
        }
    }
}
