package com.morshues.lazyathome.ui.tg

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.data.repository.TgVideoRepository
import com.morshues.lazyathome.ui.common.IVideoListModel

class TgVideoViewModel : ViewModel(), IVideoListModel {
    private val repository = TgVideoRepository()
    private val _dataList = MutableLiveData<List<TgVideoItem>>()
    val dataList: LiveData<List<TgVideoItem>> get() = _dataList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadData() {
        repository.fetchVideoList(
            onSuccess = { data -> _dataList.postValue(data) },
            onError = { error -> _errorMessage.postValue(error) }
        )
    }
}