package com.colman.dreamcatcher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost

class FeedViewModel : ViewModel() {

    val posts: LiveData<List<DreamPost>> = DreamCatcherModel.getAllPostsLocal()
    val loadingState = MutableLiveData(LoadingState.IDLE)

    fun loadPosts() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.refreshPosts { error: String? ->
            loadingState.value = if (error == null) LoadingState.SUCCESS else LoadingState.ERROR
        }
    }
}
