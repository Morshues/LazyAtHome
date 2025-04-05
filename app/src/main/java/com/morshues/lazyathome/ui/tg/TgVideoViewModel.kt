package com.morshues.lazyathome.ui.tg

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.data.repository.TgVideoRepository
import com.morshues.lazyathome.ui.common.IVideoListModel
import com.morshues.lazyathome.player.IPlayable
import com.morshues.lazyathome.player.StaticPlayableItem

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

    override fun getPlayableList(): List<IPlayable> {
        val result = mutableListOf<IPlayable>()
        _dataList.value?.forEach {
            result.add(StaticPlayableItem(it.url, it.filename))
        }
        return result
    }

    override fun getIndexOf(item: Any): Int {
        return if (item is TgVideoItem) {
            _dataList.value?.indexOf(item) ?: -1
        } else {
            -1
        }
    }

    fun toPlayableList(item: TgVideoItem): List<IPlayable> {
        return listOf(StaticPlayableItem(item.url, item.filename))
    }
}