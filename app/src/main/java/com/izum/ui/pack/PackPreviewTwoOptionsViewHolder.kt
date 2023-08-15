package com.izum.ui.pack

import com.izum.databinding.ItemPackPreviewTwoOptionsBinding
import com.izum.ui.BaseViewHolder

class PackPreviewTwoOptionsViewHolder(
    private val binding: ItemPackPreviewTwoOptionsBinding
) : BaseViewHolder<PackPreviewItem>(binding.root) {

    override fun bind(item: PackPreviewItem) {
        super.bind(item)

        binding.tvTop.text = item.topText
        binding.tvBottom.text = item.bottomText
    }

}