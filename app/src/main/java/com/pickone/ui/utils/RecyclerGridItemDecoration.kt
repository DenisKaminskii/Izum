package com.pickone.ui.utils

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import com.pickone.ui.packs.PacksAdapter

class RecyclerGridItemDecoration(
        @Px private val vertical: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val lastPosition = parent.adapter?.itemCount?.minus(1) ?: 0
        val viewType = parent.adapter?.getItemViewType(position)
        val isFirstHeader = parent.adapter?.getItemViewType(0) == PacksAdapter.ViewType.HEADER.ordinal

        if (position < 0 || position > lastPosition) return

        when {
            position < 0 || position > lastPosition -> return
            position in (0..1) -> outRect.top = vertical
            position == 2 && isFirstHeader -> outRect.top = vertical
            position in (lastPosition - 1..lastPosition) -> outRect.bottom = vertical
        }

        if (viewType == PacksAdapter.ViewType.HEADER.ordinal) {
            outRect.left = vertical
            outRect.right = vertical
        }
    }

}