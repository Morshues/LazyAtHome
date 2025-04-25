package com.morshues.lazyathome.ui.linkpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.repository.LinkPageRepository

class LinkPageViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LinkPageViewModel(LinkPageRepository(api)) as T
    }
}
