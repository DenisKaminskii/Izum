package com.polleo.ui.poll.list

import com.polleo.databinding.ItemPollsHeaderBinding
import com.polleo.ui.BaseViewHolder

class PollsHeaderViewHolder(
    private val binding: ItemPollsHeaderBinding
) : BaseViewHolder<PollsItem.Header>(binding.root) {

    override fun bind(item: PollsItem.Header) {
        super.bind(item)

        binding.root.text = item.title
    }
}