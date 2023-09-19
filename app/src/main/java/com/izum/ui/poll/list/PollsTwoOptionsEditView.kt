package com.izum.ui.poll.list

import com.izum.databinding.ItemPollsTwoOptionsEditBinding
import com.izum.ui.BaseViewHolder

class PollsTwoOptionsEditViewView(
    private val binding: ItemPollsTwoOptionsEditBinding,
    private val onClick: (Long) -> Unit,
    private val onRemoveClick: (Long) -> Unit
) : BaseViewHolder<PollsItem.TwoOptionsEdit>(binding.root) {

    override fun bind(item: PollsItem.TwoOptionsEdit) {
        super.bind(item)
        binding.tvLeft.text = item.left
        binding.tvRight.text = item.right
        binding.ivDelete.setOnClickListener {
            onRemoveClick(item.id)
        }
        binding.root.setOnClickListener {
            onClick(item.id)
        }
    }

    override fun unbind() {
        super.unbind()
        binding.ivDelete.setOnClickListener(null)
    }

}