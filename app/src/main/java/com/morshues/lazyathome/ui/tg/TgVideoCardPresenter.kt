package com.morshues.lazyathome.ui.tg

import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.ui.common.BaseCardPresenter

class TgVideoCardPresenter : BaseCardPresenter() {
    override fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder) {
        if (item is TgVideoItem) {
            cardView.titleText = item.filename
            cardView.contentText = item.durationStr
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            cardView.mainImageView?.let { view ->
                Glide.with(viewHolder.view.context)
                    .load(item.thumbnail)
                    .centerCrop()
                    .error(getDefaultCardImage())
                    .into(view)
            }
        }
    }
}