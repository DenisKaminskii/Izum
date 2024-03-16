package com.pickone.ui.packs

import com.pickone.databinding.ItemPackHeaderBinding
import com.pickone.ui.BaseViewHolder

class PackHeaderViewHolder(
    private val binding: ItemPackHeaderBinding
) : BaseViewHolder<PacksItem.Header>(binding.root) {

    override fun bind(item: PacksItem.Header) {
        super.bind(item)
        binding.tvTitle.text = item.title
    }

}