package com.izum.ui.packs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import com.izum.data.Pack
import com.izum.databinding.ViewHolderPackBinding
import com.izum.ui.BaseViewHolder
import com.izum.ui.dpF
import com.izum.ui.getBackgroundGradient

class PackViewHolder(
    private val binding: ViewHolderPackBinding,
    @ColorRes private val color: Int,
    private val onClick: (Pack) -> Unit
) : BaseViewHolder<Pack>(binding.root) {

    override fun bind(item: Pack) {
        super.bind(item)

        binding.vgRoot.background = getBackgroundGradient(
            color = itemView.context.getColor(color)
        ).apply {
            cornerRadius = itemView.context.dpF(16)
        }

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


}