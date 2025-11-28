package com.morshues.lazyathome.ui.tg

import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.R
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.settings.SettingsManager
import com.morshues.lazyathome.ui.common.BaseRowController
import com.morshues.lazyathome.ui.common.VideoPlayerActivity

class TgVideoRowController(
    title: String,
    private val activity: FragmentActivity,
    private val viewModel: TgVideoViewModel,
) : BaseRowController(viewModel) {
    private val accessToken = SettingsManager.getAccessToken(activity)?: ""
    private val cardPresenter = bindClick(TgVideoCardPresenter(accessToken))
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(0, title)

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.dataList.observe(activity) { itemList ->
            rowAdapter.setItems(itemList, null)
        }
        viewModel.errorMessage.observe(activity) { errMsg ->
            if (errMsg.isNotBlank()) {
                Toast.makeText(activity, "[TgVideo API] $errMsg", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun loadData() {
        viewModel.loadData(SettingsManager.getNSFW(activity))
    }

    override fun onClick(item: Any) {
        if (item is TgVideoItem) {
            VideoPlayerActivity.start(activity, viewModel.toPlayableList(item), 0)
        }
    }

    override fun onLongClick(item: Any, view: View): Boolean {
        if (item is TgVideoItem) {
            showPopupMenu(item, view)
        }
        return true
    }

    override fun getBackgroundUri(item: Any?): String? {
        if (item is TgVideoItem) {
            return item.thumbnail
        }
        return null
    }

    override fun buildPopupMenu(menu: Menu, item: Any) {
        menu.add(1, MENU_NSFW, 1, R.string.toggle_nsfw)
    }

    override fun onCustomMenuItemClick(itemId: Int, item: Any): Boolean {
        if (item !is TgVideoItem) return false
        return when (itemId) {
            MENU_NSFW -> {
                viewModel.toggleNSFW(item)
                true
            }
            else -> false
        }
    }

    override fun deleteItem(item: Any) {
        if (item is TgVideoItem) {
            viewModel.deleteItem(item)
        }
    }

    companion object {
        const val ID = "tg_video"
        const val MENU_NSFW = 1
    }
}