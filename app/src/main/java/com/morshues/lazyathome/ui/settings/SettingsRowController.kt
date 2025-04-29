package com.morshues.lazyathome.ui.settings

import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.R
import com.morshues.lazyathome.ui.common.BaseRowController

class SettingsRowController(
    title: String,
    activity: FragmentActivity,
    private val startSettings: () -> Unit,
) : BaseRowController() {
    private val cardPresenter = SettingsCardPresenter().apply {
        onItemClick = { item -> onClick(item) }
    }
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(999, title)

    override val listRow: ListRow

    override fun loadData() {
        /* Empty */
    }

    init {
        rowAdapter.add(SettingsItem(activity.getString(R.string.settings)))
        listRow = ListRow(header, rowAdapter)
    }

    override fun onClick(item: Any) {
        if (item is SettingsItem) {
            startSettings()
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        return null
    }
}

data class SettingsItem(
    val title: String
)