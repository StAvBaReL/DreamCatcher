package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class FeedViewModel : ViewModel() {

    val posts = MutableLiveData<List<DreamPost>>(emptyList())
    val loadingState = MutableLiveData(LoadingState.IDLE)

    fun loadPosts() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.getAllPosts(since = 0L) { list, error ->
            if (error == null) {
                posts.value = list ?: emptyList()
                loadingState.value = LoadingState.SUCCESS
            } else {
                loadingState.value = LoadingState.ERROR
            }
        }
    }
}
