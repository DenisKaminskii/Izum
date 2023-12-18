package com.pickone.ui.utils

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView

class RecyclerGridItemDecoration(
        @Px private val vertical: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val lastPosition = parent.adapter?.itemCount?.minus(1) ?: 0

        if (position < 0 || position > lastPosition) return

        when {
            position < 0 || position > lastPosition -> return
            position in (0..1) -> outRect.top = vertical
            position in (lastPosition - 1..lastPosition) -> outRect.bottom = vertical
        }
    }

}