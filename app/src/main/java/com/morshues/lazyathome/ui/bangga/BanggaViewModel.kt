package com.morshues.lazyathome.ui.bangga

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.lazyathome.data.model.BanggaAnimationItem
import com.morshues.lazyathome.data.model.BanggaCategoryItem
import com.morshues.lazyathome.data.model.BanggaDisplayable
import com.morshues.lazyathome.data.repository.BanggaRepository
import kotlinx.coroutines.launch

class BanggaViewModel : ViewModel() {
    private val repository = BanggaRepository()

    private val _categoryList = MutableLiveData<List<BanggaCategoryItem>>()
    private val _animationItem = MutableLiveData<BanggaAnimationItem?>()
    private val _displayList = MutableLiveData<List<BanggaDisplayable>>()
    val displayList: LiveData<List<BanggaDisplayable>> = _displayList

    private val _videoLink = MutableLiveData<String>()
    val videoLink: LiveData<String> = _videoLink

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    val canGoBack: Boolean
        get() = _animationItem.value != null

    fun loadData() {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.fetchCategoryItems()
            result
                .onSuccess {
                    _categoryList.value = it
                    _displayList.value = it
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun setCategory(id: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.fetchAnimationItem(id)
            result
                .onSuccess {
                    _animationItem.value = it
                    _displayList.value = it.episodes
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun goBack() {
        _animationItem.value = null
        _displayList.value = _categoryList.value
    }

    fun getVideo(id: String) {
        viewModelScope.launch {
            val result = repository.fetchVideoItem(id)
            result
                .onSuccess {
                    _videoLink.value = it.video.url
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }
}