package com.morshues.lazyathome.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.data.repository.LibraryRepository
import com.morshues.lazyathome.ui.common.IVideoListModel
import com.morshues.lazyathome.player.IPlayable
import com.morshues.lazyathome.player.StaticPlayableItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel(), IVideoListModel {
    private lateinit var rootList: List<LibraryItem>
    private val _displayList = MutableLiveData<List<LibraryItem>>()
    val displayList: LiveData<List<LibraryItem>> get() = _displayList
    private val backwardItemStack = ArrayDeque<List<LibraryItem>>()

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    override val canGoBack: Boolean
        get() = backwardItemStack.isNotEmpty()

    fun loadData() {
        repository.fetchLibraryList(
            onSuccess = { data ->
                backwardItemStack.clear()
                rootList = data
                _displayList.postValue(data)
            },
            onError = { error -> _errorMessage.postValue(error) }
        )
    }

    fun enterFolder(item: LibraryItem.FolderItem) {
        displayList.value?.let { backwardItemStack.add(it) }
        _displayList.postValue(item.children)
    }

    override fun goBack() {
        val lastList = backwardItemStack.removeLastOrNull() ?: rootList
        _displayList.postValue(lastList)
    }

    override fun getPlayableList(): List<IPlayable> {
        val result = mutableListOf<IPlayable>()
        _displayList.value?.forEach {
            if (it is LibraryItem.VideoItem) {
                result.add(StaticPlayableItem(it.url, it.name))
            }
        }
        return result
    }

    override fun getIndexOf(item: Any): Int {
        return if (item is LibraryItem.VideoItem) {
            _displayList.value?.filterIsInstance<LibraryItem.VideoItem>()?.indexOf(item) ?: -1
        } else {
            -1
        }
    }
}