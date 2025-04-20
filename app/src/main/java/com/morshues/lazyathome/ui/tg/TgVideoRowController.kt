package com.morshues.lazyathome.ui.tg

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.ui.common.BaseRowController
import com.morshues.lazyathome.ui.common.VideoPlayerActivity

class TgVideoRowController(
    title: String,
    private val activity: FragmentActivity,
    private val viewModel: TgVideoViewModel,
) : BaseRowController(viewModel) {
    private val cardPresenter = TgVideoCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(0, title)

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.dataList.observe(activity) { itemList ->
            for (tgItem in itemList) {
                rowAdapter.add(tgItem)
            }
        }
        viewModel.errorMessage.observe(activity) { errMsg ->
            Toast.makeText(activity, "[TgVideo API] $errMsg", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun loadData() {
        viewModel.loadData()
    }

    override fun onClick(item: Any) {
        if (item is TgVideoItem) {
            VideoPlayerActivity.start(activity, viewModel.toPlayableList(item), 0)
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        if (item is TgVideoItem) {
            return item.thumbnail
        }
        return null
    }

    companion object {
        const val ID = "tg_video"
    }
}