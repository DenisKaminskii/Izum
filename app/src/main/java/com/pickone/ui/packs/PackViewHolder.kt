package com.pickone.ui.packs

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import androidx.core.view.isVisible
import com.pickone.data.Pack
import com.pickone.databinding.ItemPackBinding
import com.pickone.ui.BaseViewHolder
import com.pickone.ui.dpF

class PackViewHolder(
    private val binding: ItemPackBinding,
    private val onClick: (Pack) -> Unit
) : BaseViewHolder<PacksItem.Pack>(binding.root) {

    override fun bind(item: PacksItem.Pack) {
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

        binding.tvTitle.text = item.title
        binding.tvTitle.setTextColor(item.contentColor)
        binding.tvDescription.text = item.description
        binding.tvDescription.setTextColor(item.contentColor)
        binding.tvPolls.setTextColor(item.contentColor)
        binding.tvPolls.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(item.contentColor)
        )

        binding.ivLock.isVisible = item.isPaid && !item.hasSubscription
        binding.ivLock.setColorFilter(item.contentColor)

        val pollsCommonCount = item.pollsCount

        binding.tvPolls.text = pollsCommonCount.toString()

        binding.root.setOnClickListener {
            onClick(item.pack)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnContextClickListener(null)
    }


}