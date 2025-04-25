package com.morshues.lazyathome.ui.linkpage

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.ui.common.BaseRowController

class LinkPageRowController(
    title: String,
    private val activity: FragmentActivity,
    private val viewModel: LinkPageViewModel,
) : BaseRowController() {
    private val cardPresenter = LinkPageCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(0, title)

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.displayList.observe(activity) { itemList ->
            val uiList = mutableListOf<Any?>()
            uiList.addAll(itemList.reversed())
            rowAdapter.setItems(uiList, null)
        }
        viewModel.errorMessage.observe(activity) { errMsg ->
            Toast.makeText(activity, "[LinkPage API] $errMsg", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun loadData() {
        viewModel.loadData()
    }

    override fun onClick(item: Any) {
        when (item) {
            is LinkPage -> {
                LinkPageActivity.start(activity, item.url)
            }
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        return null
    }

    companion object {
        const val ID = "link_page"
    }
}