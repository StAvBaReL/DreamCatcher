package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost
import com.google.firebase.firestore.DocumentSnapshot

class FeedViewModel : ViewModel() {

    val posts = MutableLiveData<List<DreamPost>>(emptyList())
    val loadingState = MutableLiveData(LoadingState.IDLE)
    val isLoadingMore = MutableLiveData(false)
    val isEndReached = MutableLiveData(false)

    val currentUserId: String = DreamCatcherModel.getCurrentUser()?.uid ?: ""

    private var lastSnapshot: DocumentSnapshot? = null
    private var isLastPage = false

    fun loadFirstPage() {
        lastSnapshot = null
        isLastPage = false
        isEndReached.value = false
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.getPostsPaged(limit = 10, after = null) { list, snapshot, error ->
            if (error == null) {
                val result = list ?: emptyList()
                posts.value = result
                lastSnapshot = snapshot
                if (result.size < 10) {
                    isLastPage = true
                    isEndReached.value = true
                }
                loadingState.value = LoadingState.SUCCESS
            } else {
                loadingState.value = LoadingState.ERROR
            }
        }
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

    fun loadNextPage() {
        if (isLastPage || isLoadingMore.value == true || lastSnapshot == null) return
        isLoadingMore.value = true
        DreamCatcherModel.getPostsPaged(limit = 10, after = lastSnapshot) { list, snapshot, error ->
            if (error == null) {
                val result = list ?: emptyList()
                posts.value = (posts.value ?: emptyList()) + result
                lastSnapshot = snapshot
                if (result.size < 10) {
                    isLastPage = true
                    isEndReached.value = true
                }
            }
            isLoadingMore.value = false
        }
    }
}
