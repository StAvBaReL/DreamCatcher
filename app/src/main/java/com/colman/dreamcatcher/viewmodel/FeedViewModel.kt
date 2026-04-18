package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost
import com.google.firebase.firestore.DocumentSnapshot

class FeedViewModel : ViewModel() {

    val posts: LiveData<List<DreamPost>> = DreamCatcherModel.getAllPostsLocal()
    val loadingState = MutableLiveData(LoadingState.IDLE)
    val isLoadingMore = MutableLiveData(false)
    val isEndReached = MutableLiveData(true)

    fun loadFirstPage() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.refreshPosts()
        loadingState.value = LoadingState.SUCCESS
    }

    fun loadNextPage() {
        // Pagination logic is disabled as we fetch latest changes locally.
    }
}
