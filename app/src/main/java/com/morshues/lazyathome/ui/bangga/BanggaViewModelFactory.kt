package com.morshues.lazyathome.ui.bangga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BanggaViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BanggaViewModel() as T
    }
}
