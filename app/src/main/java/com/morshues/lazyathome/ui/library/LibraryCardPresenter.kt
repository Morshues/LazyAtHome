package com.morshues.lazyathome.ui.library

import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.ui.common.BaseCardPresenter

class LibraryCardPresenter : BaseCardPresenter() {
    override fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder) {
        if (item is LibraryItem) {
            cardView.titleText = item.name
            cardView.contentText = item.name
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            if (item is LibraryItem.VideoItem) {
                Glide.with(viewHolder.view.context)
                    .load(item.src)
                    .centerCrop()
                    .error(getDefaultCardImage())
                    .into(cardView.mainImageView)
            }
        }
    }
}