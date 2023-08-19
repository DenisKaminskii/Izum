package com.izum.ui.packs

import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.izum.data.Pack
import com.izum.databinding.ItemPackBinding
import com.izum.ui.BaseViewHolder
import com.izum.ui.dpF

class PackViewHolder(
    private val binding: ItemPackBinding,
    @ColorRes private val color: Int,
    private val onClick: (Pack) -> Unit
) : BaseViewHolder<Pack>(binding.root) {

    override fun bind(item: Pack) {
        super.bind(item)
        val context = itemView.context

        val startGradient = item.colors.gradientStartColor
        val endGradient = item.colors.gradientEndColor

        binding.root.background = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(startGradient, endGradient)
        ).apply {
            cornerRadius = context.dpF(20)
        }

        binding.tvTitle.setTextColor(item.colors.contentColor)
        binding.tvPollsCount.setTextColor(item.colors.contentColor)

        binding.vLock.isVisible = item.isPaid // && !hasSubscription
        binding.tvTitle.text = item.title
        binding.tvPollsCount.text = "${item.pollsCount} polls"
        binding.root.setOnClickListener {
            onClick(item)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnContextClickListener(null)
    }


}