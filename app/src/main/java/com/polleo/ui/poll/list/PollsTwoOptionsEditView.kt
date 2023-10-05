package com.polleo.ui.poll.list

import androidx.core.view.isVisible
import com.polleo.R
import com.polleo.databinding.ItemPollsTwoOptionsEditBinding
import com.polleo.ui.BaseViewHolder

class PollsTwoOptionsEditViewView(
    private val binding: ItemPollsTwoOptionsEditBinding,
    private val onContentClick: (Long) -> Unit,
    private val onItemSelected: (Long) -> Unit
) : BaseViewHolder<PollsItem.TwoOptionsEdit>(binding.root) {

    override fun bind(item: PollsItem.TwoOptionsEdit) {
        super.bind(item)
        with(binding) {
            tvLeft.text = item.left
            tvRight.text = item.right
            vgContent.setOnClickListener {
                onContentClick(item.id)
            }
            ivDelete.setOnClickListener {
                onItemSelected(item.id)
            }
            ivDelete.isVisible = item.isSelected != null
            if (item.isSelected != null) {
                ivDelete.setImageResource(
                    if (item.isSelected) R.drawable.ic_edit_pack_checked
                    else R.drawable.ic_edit_pack_item_unchecked
                )
            }
        }
    }

    override fun unbind() {
        super.unbind()
        binding.ivDelete.setOnClickListener(null)
    }

}