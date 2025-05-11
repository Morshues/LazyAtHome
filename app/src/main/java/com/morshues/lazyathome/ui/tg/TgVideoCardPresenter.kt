package com.morshues.lazyathome.ui.tg

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import com.bumptech.glide.Glide
import com.morshues.lazyathome.R
import com.morshues.lazyathome.data.model.TgVideoItem
import com.morshues.lazyathome.ui.common.BaseCardPresenter
import kotlin.properties.Delegates

class TgVideoCardPresenter : BaseCardPresenter() {
    private var sNSFWDefaultBackgroundColor: Int by Delegates.notNull()
    private var sNSFWSelectedBackgroundColor: Int by Delegates.notNull()

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        sNSFWDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background_nsfw)
        sNSFWSelectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.selected_background_nsfw)
        return super.onCreateViewHolder(parent)
    }

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

    override fun getBackgroundColor(item: Any?, selected: Boolean): Int {
        return if (item is TgVideoItem && item.nsfw) {
            if (selected) sNSFWSelectedBackgroundColor else sNSFWDefaultBackgroundColor
        } else {
            super.getBackgroundColor(item, selected)
        }
    }

}