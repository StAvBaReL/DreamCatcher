package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class FeedViewModel : ViewModel() {

    val posts: LiveData<PagingData<DreamPost>> = DreamCatcherModel.allPosts.cachedIn(viewModelScope)
    val loadingState = MutableLiveData(LoadingState.IDLE)
    val currentUserId: String = DreamCatcherModel.getCurrentUser()?.uid ?: ""

    fun loadFirstPage() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.refreshPosts()
        loadingState.value = LoadingState.SUCCESS
    }

    fun toggleLike(post: DreamPost) {
        val uid = currentUserId.ifEmpty { return }
        val isLiked = uid in post.likes
        val updatedLikes = if (isLiked) post.likes - uid else post.likes + uid
        val updatedPost = post.copy(likes = updatedLikes)
        
        posts.value = posts.value?.map { if (it.postId == post.postId) updatedPost else it }
        
        DreamCatcherModel.toggleLike(updatedPost, uid, isLiked) { success ->
            if (!success) {
                posts.value = posts.value?.map { if (it.postId == post.postId) post else it }
            }
        }
    }
}
 