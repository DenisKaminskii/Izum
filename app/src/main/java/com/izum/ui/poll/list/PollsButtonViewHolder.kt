package com.izum.ui.poll.list

import com.izum.databinding.ItemPollsButtonBinding
import com.izum.ui.BaseViewHolder

class PollsButtonViewHolder(
    private val binding: ItemPollsButtonBinding,
    val onClick: () -> Unit
) : BaseViewHolder<PollsItem.Button>(binding.root) {

    override fun bind(item: PollsItem.Button) {
        super.bind(item)

        binding.tvAction.text = item.title
        binding.tvAction.setOnClickListener { onClick() }
    }

    override fun unbind() {
        super.unbind()
        binding.tvAction.setOnClickListener(null)
    }
}