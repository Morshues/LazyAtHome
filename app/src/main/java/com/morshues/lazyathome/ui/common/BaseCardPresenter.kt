package com.morshues.lazyathome.ui.common

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.morshues.lazyathome.R
import kotlin.properties.Delegates

abstract class BaseCardPresenter : Presenter() {
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
        }.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            updateCardBackgroundColor(this, false)
        }

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        if (item == null) {
            return
        }

        Log.d(TAG, "onBindViewHolder")
        val cardView = viewHolder.view as ImageCardView

        if (item is GoBackUIItem) {
            cardView.titleText = "← Go Back"
            cardView.contentText = ""
            return
        }

        onBindCard(cardView, item, viewHolder)
    }

    abstract fun onBindCard(cardView: ImageCardView, item: Any, viewHolder: ViewHolder)

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        Log.d(TAG, "onUnbindViewHolder")
        val cardView = viewHolder.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    protected fun getDefaultCardImage(): Drawable? = mDefaultCardImage

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
        // Both background colors should be set because the view"s background is temporarily visible
        // during animations.
        view.setBackgroundColor(color)
        view.setInfoAreaBackgroundColor(color)
    }

    companion object {
        private const val TAG = "BaseCardPresenter"

        const val CARD_WIDTH = 384
        const val CARD_HEIGHT = 216
    }
}