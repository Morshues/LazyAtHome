package com.morshues.lazyathome.ui.common

import android.view.View
import android.widget.PopupMenu
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.R

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
    open fun onLongClick(item: Any, view: View): Boolean {
        return false
    }
    abstract fun getBackgroundUri(item: Any?): String?

    protected fun <P : BaseCardPresenter> bindClick(presenter: P): P {
        presenter.onItemClick = { item -> onClick(item) }
        presenter.onItemLongClick = { item, view -> onLongClick(item, view) }
        return presenter
    }

    protected open fun showPopupMenu(item: Any, anchorView: View) {
        val popupMenu = PopupMenu(anchorView.context, anchorView)
        popupMenu.menu.apply {
            add(99, MENU_DELETE, 99, R.string.delete)
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                MENU_DELETE -> {
                    deleteItem(item)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    protected open fun deleteItem(item: Any) {}

    private companion object {
        const val MENU_DELETE = 99
    }
}
