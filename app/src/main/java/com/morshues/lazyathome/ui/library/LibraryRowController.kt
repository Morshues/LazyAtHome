package com.morshues.lazyathome.ui.library

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.ui.common.GoBackUIItem
import com.morshues.lazyathome.ui.main.VideoPlayerActivity

class LibraryRowController(
    private val activity: FragmentActivity,
    private val viewModel: LibraryViewModel,
) {
    private val cardPresenter = LibraryCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(0, "Library")

    val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.displayList.observe(activity) { itemList ->
            val uiList = mutableListOf<Any>()
            if (viewModel.canGoBack) {
                uiList.add(GoBackUIItem)
            }
            uiList.addAll(itemList.reversed())
            rowAdapter.setItems(uiList, null)
        }
        viewModel.loadData()
    }

    fun onClick(item: Any) {
        when (item) {
            is LibraryItem.VideoItem -> {
                VideoPlayerActivity.start(activity, item.url)
            }
            is LibraryItem.FolderItem -> {
                viewModel.enterFolder(item)
            }
            is GoBackUIItem -> {
                viewModel.goBack()
            }
        }
    }

    fun getBackgroundUri(item: Any?): String? {
        if (item is LibraryItem.VideoItem) {
            return item.src
        }
        return null
    }
}