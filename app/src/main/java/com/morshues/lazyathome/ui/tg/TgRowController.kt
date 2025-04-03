package com.morshues.lazyathome.ui.tg

import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.ui.common.RowController
import com.morshues.lazyathome.ui.common.VideoPlayerActivity

class TgRowController(
    private val activity: FragmentActivity,
    viewModel: TgVideoViewModel,
) : RowController(viewModel) {
    private val cardPresenter = TgVideoCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(1, "TG Videos")

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.dataList.observe(activity) { itemList ->
            for (tgItem in itemList) {
                rowAdapter.add(tgItem)
            }
        }

        viewModel.loadData()
    }

    override fun onClick(item: Any) {
        if (item is TgVideoItem) {
            VideoPlayerActivity.start(activity, item.url)
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        return null
    }
}