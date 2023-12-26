package com.pickone.ui.poll.list

import com.pickone.databinding.ItemPackHeaderBinding
import com.pickone.ui.BaseViewHolder

class PollsHeaderViewHolder(
    private val binding: ItemPackHeaderBinding
) : BaseViewHolder<PollsItem.Header>(binding.root) {

    override fun bind(item: PollsItem.Header) {
        super.bind(item)

        binding.tvTitle.text = item.title
    }
}