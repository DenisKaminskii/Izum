package com.polleo.ui.pack

import com.polleo.databinding.ItemPackPreviewTwoOptionsBinding
import com.polleo.ui.BaseViewHolder

class PackPreviewTwoOptionsViewHolder(
    private val binding: ItemPackPreviewTwoOptionsBinding
) : BaseViewHolder<PackPreviewItem>(binding.root) {

    override fun bind(item: PackPreviewItem) {
        super.bind(item)

        binding.tvTop.text = item.topText
        binding.tvBottom.text = item.bottomText
        binding.tvTop.setTextColor(item.textColor)
        binding.tvBottom.setTextColor(item.textColor)
    }

}