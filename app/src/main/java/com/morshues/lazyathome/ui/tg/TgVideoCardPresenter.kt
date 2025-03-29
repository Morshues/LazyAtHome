package com.morshues.lazyathome.ui.tg

import androidx.leanback.widget.ImageCardView
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.ui.common.BaseCardPresenter

class TgVideoCardPresenter : BaseCardPresenter() {
    override fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder) {
        if (item is TgVideoItem) {
            cardView.titleText = item.id
            cardView.contentText = item.id
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
//        Glide.with(viewHolder.view.context)
//            .load(tgVideo.cardImageUrl)
//            .centerCrop()
//            .error(getDefaultCardImage())
//            .into(cardView.mainImageView)
        }
    }
}