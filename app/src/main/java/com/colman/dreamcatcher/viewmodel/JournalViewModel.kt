package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class JournalViewModel : ViewModel() {

    private val authUserUid = DreamCatcherModel.getCurrentUser()?.uid ?: ""

    val loadingState = MutableLiveData(LoadingState.IDLE)
    val posts: LiveData<List<DreamPost>> = DreamCatcherModel.getPostsByUserLocal(authUserUid)

    fun loadPosts() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.refreshPosts { error: String? ->
            loadingState.value = if (error == null) LoadingState.SUCCESS else LoadingState.ERROR
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
