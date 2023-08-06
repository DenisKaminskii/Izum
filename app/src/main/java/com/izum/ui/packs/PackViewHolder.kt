package com.izum.ui.packs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.core.view.isVisible
import com.izum.data.Pack
import com.izum.databinding.ViewHolderPackBinding
import com.izum.ui.BaseViewHolder
import com.izum.ui.dpF

class PackViewHolder(
    private val binding: ViewHolderPackBinding,
    private val color: Int,
    private val onClick: (Pack) -> Unit
) : BaseViewHolder<Pack>(binding.root) {

    override fun bind(item: Pack) {
        super.bind(item)

        binding.vgRoot.background = getBackgroundGradient(
            itemView.context.getColor(color)
        )

        binding.vLock.isVisible = item.isPaid // && !hasSubscription
        binding.vTitle.text = item.title
        binding.vPollsCount.text = "${item.pollsCount} polls"
        binding.root.setOnClickListener {
            onClick(item)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnContextClickListener(null)
    }

    private fun getBackgroundGradient(color: Int): GradientDrawable {
        val deltaRed = 35
        val deltaGreen = 22
        val deltaBlue = 14

        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        val darkerRed = (red - deltaRed).coerceAtLeast(0)
        val darkerGreen = (green - deltaGreen).coerceAtLeast(0)
        val darkerBlue = (blue - deltaBlue).coerceAtLeast(0)

        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color, Color.rgb(darkerRed, darkerGreen, darkerBlue))
        ).apply {
            cornerRadius = itemView.context.dpF(16)
        }

        return gradientDrawable
    }

}