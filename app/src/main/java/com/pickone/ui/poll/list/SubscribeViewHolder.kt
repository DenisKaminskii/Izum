package com.pickone.ui.poll.list

import com.pickone.databinding.ItemSubscribeBinding
import com.pickone.ui.BaseViewHolder

class SubscribeViewHolder(
    private val binding: ItemSubscribeBinding,
    val onClick: () -> Unit
) : BaseViewHolder<PollsItem.Subscribe>(binding.root) {

    override fun bind(item: PollsItem.Subscribe) {
        super.bind(item)

        binding.tvSubscribe.setOnClickListener { onClick() }
    }

    override fun unbind() {
        super.unbind()
        binding.tvSubscribe.setOnClickListener(null)
    }
}