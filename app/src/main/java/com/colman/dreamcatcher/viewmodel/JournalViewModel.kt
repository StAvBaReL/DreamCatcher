package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class JournalViewModel : ViewModel() {

    private val authUserUid: String
        get() = DreamCatcherModel.getCurrentUser()?.uid ?: ""

    val loadingState = MutableLiveData(LoadingState.IDLE)

    val posts: LiveData<PagingData<DreamPost>> by lazy {
        DreamCatcherModel.getPostsByUserLocal(authUserUid).cachedIn(viewModelScope)
    }

    fun loadPosts() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.refreshPosts()
        loadingState.value = LoadingState.SUCCESS
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
