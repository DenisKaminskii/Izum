package com.izum.ui.poll.statistic

import com.izum.databinding.ItemStatisticHeaderBinding
import com.izum.ui.BaseViewHolder

class StatisticHeaderViewHolder(
    private val binding: ItemStatisticHeaderBinding
) : BaseViewHolder<StatisticItem.Header>(binding.root) {

    override fun bind(item: StatisticItem.Header) {
        super.bind(item)

        binding.root.text = item.title
    }
}