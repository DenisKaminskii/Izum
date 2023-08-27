package com.izum.ui.packs

import android.graphics.drawable.GradientDrawable
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

        val startGradient = item.gradientStartColor
        val endGradient = item.gradientEndColor

        binding.root.background = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(startGradient, endGradient)
        ).apply {
            cornerRadius = context.dpF(20)
        }

        binding.tvTitle.setTextColor(item.contentColor)
        binding.tvPollsCount.setTextColor(item.contentColor)

        binding.ivLock.isVisible = item.isPaid && !item.hasSubscription
        binding.ivLock.setColorFilter(item.contentColor)
        binding.tvTitle.text = item.title
        binding.tvPollsCount.text = "${item.pollsCount} polls"
        binding.root.setOnClickListener {
            onClick(item.pack)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnContextClickListener(null)
    }


}