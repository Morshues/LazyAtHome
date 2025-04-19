package com.morshues.lazyathome.ui.common

import androidx.leanback.widget.ListRow

abstract class BaseRowController(
    private val videoListModel: IVideoListModel? = null,
) {
    abstract val listRow: ListRow
    abstract fun loadData()
    fun handleBackPress(): Boolean {
        videoListModel?.apply {
            if (canGoBack) {
                goBack()
                return true
            }
        }
        return false
    }
    abstract fun onClick(item: Any)
    abstract fun getBackgroundUri(item: Any?): String?
}
