package com.morshues.lazyathome.ui.linkpage

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import com.morshues.lazyathome.R
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.ui.common.BaseCardPresenter
import kotlin.properties.Delegates

class LinkPageCardPresenter : BaseCardPresenter() {
    private var sNSFWDefaultBackgroundColor: Int by Delegates.notNull()
    private var sNSFWSelectedBackgroundColor: Int by Delegates.notNull()

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        sNSFWDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background_nsfw)
        sNSFWSelectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.selected_background_nsfw)
        return super.onCreateViewHolder(parent)
    }

    override fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder) {
        if (item is LinkPage) {
            cardView.titleText = item.title.ifBlank { "(No Title)" }
            cardView.contentText = item.url
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        }
    }

    override fun getBackgroundColor(item: Any?, selected: Boolean): Int {
        return if (item is LinkPage && item.nsfw) {
            if (selected) sNSFWSelectedBackgroundColor else sNSFWDefaultBackgroundColor
        } else {
            super.getBackgroundColor(item, selected)
        }
    }
}