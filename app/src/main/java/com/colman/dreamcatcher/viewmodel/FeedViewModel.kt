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

    fun loadFirstPage() {
        loadingState.value = LoadingState.LOADING
        DreamCatcherModel.refreshPosts()
        loadingState.value = LoadingState.SUCCESS
    }
}
 