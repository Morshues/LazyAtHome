package com.morshues.lazyathome.ui.bangga

import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import com.morshues.lazyathome.data.model.BanggaCategoryItem
import com.morshues.lazyathome.data.model.BanggaDisplayable
import com.morshues.lazyathome.data.model.BanggaEpisode
import com.morshues.lazyathome.ui.common.GoBackUIItem
import com.morshues.lazyathome.ui.common.BaseRowController
import com.morshues.lazyathome.ui.common.VideoPlayerActivity

class BanggaRowController(
    title: String,
    private val activity: FragmentActivity,
    private val viewModel: BanggaViewModel,
) : BaseRowController(viewModel) {
    private val cardPresenter = BanggaCardPresenter()
    private val rowAdapter = ArrayObjectAdapter(cardPresenter)
    private val header = HeaderItem(0, title)

    override val listRow: ListRow = ListRow(header, rowAdapter)

    init {
        viewModel.displayList.observe(activity) { itemList ->
            val uiList = mutableListOf<Any?>()
            if (viewModel.canGoBack) {
                uiList.add(GoBackUIItem)
            }
            uiList.addAll(itemList)
            rowAdapter.setItems(uiList, null)
        }
        viewModel.errorMessage.observe(activity) { errMsg ->
            errMsg?.run {
                Toast.makeText(activity, "[Bangga API] $errMsg", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun loadData() {
        viewModel.loadData()
    }

    override fun onClick(item: Any) {
        when (item) {
            is BanggaCategoryItem -> viewModel.setCategory(item.id)
            is BanggaEpisode -> {
                VideoPlayerActivity.start(activity, viewModel.getPlayableList(), viewModel.getIndexOf(item))
            }
            is GoBackUIItem -> viewModel.goBack()
        }
    }

    override fun getBackgroundUri(item: Any?): String? {
        if (item is BanggaDisplayable) {
            return item.image.url
        }
        return null
    }

    companion object {
        const val ID = "bangga"
    }
}