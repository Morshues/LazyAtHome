package com.morshues.lazyathome.ui.linkpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.data.repository.LinkPageRepository

class LinkPageViewModel(
    private val repository: LinkPageRepository
) : ViewModel() {
    private val _rootList = MutableLiveData<List<LinkPage>>()
    val displayList: LiveData<List<LinkPage>> get() = _rootList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadData() {
        repository.fetchLinkPageList(
            onSuccess = { data -> _rootList.postValue(data) },
            onError = { error -> _errorMessage.postValue(error) }
        )
    }
}