package com.morshues.lazyathome.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.repository.LibraryRepository

class LibraryViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LibraryViewModel(LibraryRepository(api)) as T
    }
}
