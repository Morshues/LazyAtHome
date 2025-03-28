package com.morshues.lazyathome.ui.library

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.morshues.lazyathome.R
import com.morshues.lazyathome.data.model.LibraryItem
import com.morshues.lazyathome.ui.common.GoBackUIItem
import kotlin.properties.Delegates

class LibraryCardPresenter : Presenter() {
    private var mDefaultCardImage: Drawable? = null
    private var sSelectedBackgroundColor: Int by Delegates.notNull()
    private var sDefaultBackgroundColor: Int by Delegates.notNull()

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        Log.d(TAG, "onCreateViewHolder")

        sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        sSelectedBackgroundColor =
            ContextCompat.getColor(parent.context, R.color.selected_background)
        mDefaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.movie)

        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }

        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        Log.d(TAG, "onBindViewHolder")
        val cardView = viewHolder.view as ImageCardView

        if (item is GoBackUIItem) {
            cardView.titleText = "← Go Back"
            cardView.contentText = ""
            cardView.setMainImageDimensions(300, 200)
            return
        } else if (item is LibraryItem) {
            cardView.titleText = item.name
            cardView.contentText = item.name
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            if (item is LibraryItem.VideoItem) {
                Glide.with(viewHolder.view.context)
                    .load(item.src)
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.mainImageView)
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        Log.d(TAG, "onUnbindViewHolder")
        val cardView = viewHolder.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
        // Both background colors should be set because the view"s background is temporarily visible
        // during animations.
        view.setBackgroundColor(color)
        view.setInfoAreaBackgroundColor(color)
    }

    companion object {
        private const val TAG = "LibraryCardPresenter"

        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }
}