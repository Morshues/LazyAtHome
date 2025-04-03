package com.morshues.lazyathome.ui.bangga

import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.BanggaCategoryItem
import com.morshues.lazyathome.data.model.BanggaDisplayable
import com.morshues.lazyathome.data.model.BanggaEpisode
import com.morshues.lazyathome.ui.common.GoBackUIItem
import com.morshues.lazyathome.ui.common.RowController
import com.morshues.lazyathome.ui.common.VideoPlayerActivity

class BanggaRowController(
    private val activity: FragmentActivity,
    private val viewModel: BanggaViewModel,
) : RowController(viewModel) {
    private val cardPresenter = BanggaCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(2, "Bangga")

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.displayList.observe(activity) { itemList ->
            val uiList = mutableListOf<Any>()
            if (viewModel.canGoBack) {
                uiList.add(GoBackUIItem)
            }
            uiList.addAll(itemList)
            rowAdapter.setItems(uiList, null)
        }
        viewModel.videoLink.observe(activity) {
            VideoPlayerActivity.start(activity, it)
        }

        viewModel.loadData()
    }

    override fun onClick(item: Any) {
        when (item) {
            is BanggaCategoryItem -> viewModel.setCategory(item.id)
            is BanggaEpisode -> viewModel.getVideo(item.id)
            is GoBackUIItem -> viewModel.goBack()
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        if (item is BanggaDisplayable) {
            return item.image.url
        }
        return null
    }
}