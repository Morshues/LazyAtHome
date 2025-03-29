package com.morshues.lazyathome.ui.common

import androidx.leanback.widget.ListRow

interface RowController {
    val listRow: ListRow
    fun handleBackPress(): Boolean = false
    fun onClick(item: Any)
    fun getBackgroundUri(item: Any?): String?
}