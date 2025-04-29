package com.morshues.lazyathome.ui.linkpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.data.repository.LinkPageRepository
import kotlinx.coroutines.launch

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

    fun deleteItem(item: LinkPage) {
        viewModelScope.launch {
            _errorMessage.value = ""
            val result = repository.deleteLinkPage(item.id)
            if (result) {
                val currentList = _rootList.value.orEmpty()
                val updatedList = currentList.filterNot { it.id == item.id }
                _rootList.postValue(updatedList)
            } else {
                _errorMessage.postValue("Delete Failed")
            }
        }
    }
}