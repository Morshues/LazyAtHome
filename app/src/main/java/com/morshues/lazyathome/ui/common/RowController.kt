package com.morshues.lazyathome.ui.common

import androidx.leanback.widget.ListRow

interface RowController {
    val listRow: ListRow
    fun onClick(item: Any)
    fun getBackgroundUri(item: Any?): String?
}