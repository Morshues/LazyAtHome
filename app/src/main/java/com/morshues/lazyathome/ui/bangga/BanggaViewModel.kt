package com.morshues.lazyathome.ui.bangga

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morshues.lazyathome.data.model.BanggaAnimationItem
import com.morshues.lazyathome.data.model.BanggaCategoryItem
import com.morshues.lazyathome.data.model.BanggaDisplayable
import com.morshues.lazyathome.data.model.BanggaEpisode
import com.morshues.lazyathome.data.repository.BanggaRepository
import com.morshues.lazyathome.ui.common.IVideoListModel
import com.morshues.lazyathome.player.IPlayable
import kotlinx.coroutines.launch

class BanggaViewModel : ViewModel(), IVideoListModel {
    private val repository = BanggaRepository()

    private val _categoryList = MutableLiveData<List<BanggaCategoryItem>>()
    private val _animationItem = MutableLiveData<BanggaAnimationItem?>()
    private val _displayList = MutableLiveData<List<BanggaDisplayable>>()
    val displayList: LiveData<List<BanggaDisplayable>> = _displayList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    override val canGoBack: Boolean
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

    override fun goBack() {
        _animationItem.value = null
        _displayList.value = _categoryList.value
    }

    override fun getPlayableList(): List<IPlayable> {
        val result = mutableListOf<IPlayable>()
        _animationItem.value?.episodes?.forEach {
            result.add(BanggaPlayableItem(it.id, it.title) { id ->
                val item = repository.fetchVideoItem(id).getOrElse {
                    return@BanggaPlayableItem ""
                }
                return@BanggaPlayableItem item.video.url
            })
        }
        return result
    }

    override fun getIndexOf(item: Any): Int {
        return if (item is BanggaEpisode) {
            _displayList.value?.filterIsInstance<BanggaEpisode>()?.indexOf(item) ?: -1
        } else {
            -1
        }
    }

    data class BanggaPlayableItem(
        val id: String,
        override val title: String?,
        val fetcher: suspend (String) -> String
    ) : IPlayable {
        override suspend fun resolveUrl(): String = fetcher(id)
    }
}