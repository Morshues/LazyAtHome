package com.morshues.lazyathome.ui.linkpage

import androidx.leanback.widget.ImageCardView
import com.morshues.lazyathome.data.model.LinkPage
import com.morshues.lazyathome.ui.common.BaseCardPresenter

class LinkPageCardPresenter : BaseCardPresenter() {
    override fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder) {
        if (item is LinkPage) {
            cardView.titleText = item.title.ifBlank { "(No Title)" }
            cardView.contentText = item.url
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        }
    }
}