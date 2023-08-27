package com.izum.ui.poll.list

import com.izum.databinding.ItemPollsHeaderBinding
import com.izum.ui.BaseViewHolder

class PollsHeaderViewHolder(
    private val binding: ItemPollsHeaderBinding
) : BaseViewHolder<PollsItem.Header>(binding.root) {

    override fun bind(item: PollsItem.Header) {
        super.bind(item)

        binding.root.text = item.title
    }
}