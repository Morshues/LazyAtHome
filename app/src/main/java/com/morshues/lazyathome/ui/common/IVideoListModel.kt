package com.morshues.lazyathome.ui.common

import com.morshues.lazyathome.player.IPlayable

interface IVideoListModel {
    val canGoBack: Boolean
        get() = false
    fun goBack() {}
    fun getPlayableList(): List<IPlayable>
    fun getIndexOf(item: Any): Int
}
