package com.morshues.lazyathome.ui.library

import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.ui.common.GoBackUIItem
import com.morshues.lazyathome.ui.common.BaseRowController
import com.morshues.lazyathome.ui.common.VideoPlayerActivity

class LibraryRowController(
    title: String,
    private val activity: FragmentActivity,
    private val viewModel: LibraryViewModel,
) : BaseRowController(viewModel) {
    private val cardPresenter = LibraryCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(0, title)

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.displayList.observe(activity) { itemList ->
            val uiList = mutableListOf<Any?>()
            if (viewModel.canGoBack) {
                uiList.add(GoBackUIItem)
            }
            uiList.addAll(itemList.reversed())
            rowAdapter.setItems(uiList, null)
        }
    }

    override fun loadData() {
        viewModel.loadData()
    }

    override fun onClick(item: Any) {
        when (item) {
            is LibraryItem.VideoItem -> {
                VideoPlayerActivity.start(activity, viewModel.getPlayableList(), viewModel.getIndexOf(item))
            }
            is LibraryItem.FolderItem -> {
                viewModel.enterFolder(item)
            }
            is GoBackUIItem -> {
                viewModel.goBack()
            }
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        if (item is LibraryItem.VideoItem) {
            return item.src
        }
        return null
    }

    companion object {
        const val ID = "library"
    }
}