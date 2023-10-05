package com.polleo.ui.poll.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.polleo.databinding.ItemPollsHeaderBinding
import com.polleo.databinding.ItemPollsTwoOptionsBarBinding
import com.polleo.databinding.ItemPollsTwoOptionsEditBinding
import com.polleo.databinding.ItemSubscribeBinding
import com.polleo.ui.BaseViewHolder

sealed class PollsItem {

    data class Header(
        val title: String
    ) : PollsItem()

    data class TwoOptionsBar(
        val id: Long,
        val leftTop: Value? = null,
        val rightTop: Value? = null,
        val leftBottom: Value? = null,
        val rightBottom: Value? = null,
        val barPercent: Int
    ) : PollsItem() {

        data class Value(
            val text: String,
            @ColorInt
            val color: Int
        )

    }

    data class TwoOptionsEdit(
        val id: Long,
        val left: String,
        val right: String,
        val isSelected: Boolean? = null
    ) : PollsItem()

    data class Subscribe(
        val onClick: () -> Unit
    ) : PollsItem()

}

class PollsAdapter(
    private val onCustomPackPollClick: (Long) -> Unit = {},
    private val onCustomPackPollSelected: (Long) -> Unit = {},
    private val onStatisticClick: (Long) -> Unit = {},
    private val onSubscribeClick: () -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    enum class ViewType {
        HEADER,
        TWO_OPTIONS_BAR,
        TWO_OPTIONS_EDIT,
        BUTTON
    }

    private val items = mutableListOf<PollsItem>()

    fun setItems(items: List<PollsItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position > items.lastIndex) {
            return RecyclerView.NO_POSITION
        }

        return when(items[position]) {
            is PollsItem.Header -> ViewType.HEADER.ordinal
            is PollsItem.TwoOptionsBar -> ViewType.TWO_OPTIONS_BAR.ordinal
            is PollsItem.TwoOptionsEdit -> ViewType.TWO_OPTIONS_EDIT.ordinal
            is PollsItem.Subscribe -> ViewType.BUTTON.ordinal
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when(holder) {
            is PollsHeaderViewHolder -> holder.bind(item as PollsItem.Header)
            is PollsTwoOptionsBarViewHolder -> holder.bind(item as PollsItem.TwoOptionsBar)
            is PollsTwoOptionsEditViewView -> holder.bind(item as PollsItem.TwoOptionsEdit)
            is SubscribeViewHolder -> holder.bind(item as PollsItem.Subscribe)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.HEADER.ordinal -> {
                val binding = ItemPollsHeaderBinding.inflate(inflater, parent, false)
                PollsHeaderViewHolder(binding)
            }
            ViewType.TWO_OPTIONS_BAR.ordinal -> {
                val binding = ItemPollsTwoOptionsBarBinding.inflate(inflater, parent, false)
                PollsTwoOptionsBarViewHolder(binding, onStatisticClick)
            }
            ViewType.TWO_OPTIONS_EDIT.ordinal -> {
                val binding = ItemPollsTwoOptionsEditBinding.inflate(inflater, parent, false)
                PollsTwoOptionsEditViewView(binding, onCustomPackPollClick, onCustomPackPollSelected)
            }
            ViewType.BUTTON.ordinal -> {
                val binding = ItemSubscribeBinding.inflate(inflater, parent, false)
                SubscribeViewHolder(binding, onSubscribeClick)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}