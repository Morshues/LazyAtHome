package com.morshues.lazyathome.ui.bangga

import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import com.morshues.lazyathome.data.model.BanggaDisplayable
import com.morshues.lazyathome.ui.common.BaseCardPresenter

class BanggaCardPresenter : BaseCardPresenter() {
    override fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder) {
        if (item is BanggaDisplayable) {
            cardView.titleText = item.title
            cardView.contentText = item.info
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            Glide.with(viewHolder.view.context)
                .load(item.image.url)
                .centerCrop()
                .error(getDefaultCardImage())
                .into(cardView.mainImageView)
        }
    }
}