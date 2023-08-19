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
    private val onClick: (Pack) -> Unit
) : BaseViewHolder<PacksItem>(binding.root) {

    override fun bind(item: PacksItem) {
        super.bind(item)
        val context = itemView.context
        val pack = item.pack

        val startGradient = pack.colors.gradientStartColor
        val endGradient = pack.colors.gradientEndColor

        binding.root.background = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(startGradient, endGradient)
        ).apply {
            cornerRadius = context.dpF(20)
        }

        binding.tvTitle.setTextColor(pack.colors.contentColor)
        binding.tvPollsCount.setTextColor(pack.colors.contentColor)

        binding.vLock.isVisible = pack.isPaid && !item.hasSubscription
        binding.tvTitle.text = pack.title
        binding.tvPollsCount.text = "${pack.pollsCount} polls"
        binding.root.setOnClickListener {
            onClick(pack)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnContextClickListener(null)
    }


}