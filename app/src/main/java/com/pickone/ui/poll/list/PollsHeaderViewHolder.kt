package com.pickone.ui.poll.list

import com.pickone.databinding.ItemPollsHeaderBinding
import com.pickone.ui.BaseViewHolder

class PollsHeaderViewHolder(
    private val binding: ItemPollsHeaderBinding
) : BaseViewHolder<PollsItem.Header>(binding.root) {

    override fun bind(item: PollsItem.Header) {
        super.bind(item)

        binding.root.text = item.title
    }
}