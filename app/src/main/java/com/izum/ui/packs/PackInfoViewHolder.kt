package com.izum.ui.packs

import com.izum.data.Pack
import com.izum.databinding.ViewHolderPackInfoBinding

class PackInfoViewHolder(
    private val binding: ViewHolderPackInfoBinding,
    private val onClick: (Pack) -> Unit
) : PackViewHolder<Pack>(binding.root) {


    override fun bind(item: Pack) {
        binding.vId.text = item.id.toString()
        binding.vTitle.text = item.title
        binding.vDescription.text = item.description ?: "no description"
        binding.vIsPaid.text = "isPaid: ${item.isPaid}"
        binding.vProductId.text = item.productId ?: "no product id"
        binding.vPollsCount.text = item.pollsCount.toString()

        binding.root.setOnClickListener {
            onClick(item)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnClickListener(null)
    }
}