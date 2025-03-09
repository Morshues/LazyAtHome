package com.morshues.lazyathome.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morshues.lazyathome.data.model.VideoItem
import com.morshues.lazyathome.data.repository.VideoRepository

class VideoViewModel : ViewModel() {
    private val repository = VideoRepository()
    private val _dataList = MutableLiveData<List<VideoItem>>()
    val dataList: LiveData<List<VideoItem>> get() = _dataList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadData() {
        repository.fetchVideoList(
            onSuccess = { data -> _dataList.postValue(data) },
            onError = { error -> _errorMessage.postValue(error) }
        )
    }
}