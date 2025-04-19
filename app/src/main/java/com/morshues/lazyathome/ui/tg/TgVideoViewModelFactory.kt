package com.morshues.lazyathome.ui.tg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.morshues.lazyathome.data.api.ApiService
import com.morshues.lazyathome.data.repository.TgVideoRepository

class TgVideoViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TgVideoViewModel(TgVideoRepository(api)) as T
    }
}
