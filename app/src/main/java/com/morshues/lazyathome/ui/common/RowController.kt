package com.morshues.lazyathome.ui.common

import androidx.leanback.widget.ListRow

abstract class RowController(
    private val videoListModel: IVideoListModel,
) {
    abstract val listRow: ListRow
    fun handleBackPress(): Boolean {
        if (videoListModel.canGoBack) {
            videoListModel.goBack()
            return true
        } else {
            return false
        }
    }
    abstract fun onClick(item: Any)
    abstract fun getBackgroundUri(item: Any?): String?
}