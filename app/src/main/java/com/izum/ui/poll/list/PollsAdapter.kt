package com.izum.ui.poll.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.izum.databinding.ItemPollsButtonBinding
import com.izum.databinding.ItemPollsHeaderBinding
import com.izum.databinding.ItemPollsTwoOptionsBarBinding
import com.izum.databinding.ItemPollsTwoOptionsEditBinding
import com.izum.ui.BaseViewHolder

sealed class PollsItem {

    data class Header(
        val title: String
    ) : PollsItem()

    data class TwoOptionsBar(
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
        val left: String,
        val right: String
    ) : PollsItem()

    data class Button(
        val title: String,
        val onClick: () -> Unit
    ) : PollsItem()

}

class PollsAdapter(
    private val onPollClick: () -> Unit = {},
    private val onButtonClick: () -> Unit = {}
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
            is PollsItem.Button -> ViewType.BUTTON.ordinal
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when(holder) {
            is PollsHeaderViewHolder -> holder.bind(item as PollsItem.Header)
            is PollsTwoOptionsBarViewHolder -> holder.bind(item as PollsItem.TwoOptionsBar)
            is PollsTwoOptionsEditViewView -> holder.bind(item as PollsItem.TwoOptionsEdit)
            is PollsButtonViewHolder -> holder.bind(item as PollsItem.Button)
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
                PollsTwoOptionsBarViewHolder(binding, onPollClick)
            }
            ViewType.TWO_OPTIONS_EDIT.ordinal -> {
                val binding = ItemPollsTwoOptionsEditBinding.inflate(inflater, parent, false)
                PollsTwoOptionsEditViewView(binding, onPollClick)
            }
            ViewType.BUTTON.ordinal -> {
                val binding = ItemPollsButtonBinding.inflate(inflater, parent, false)
                PollsButtonViewHolder(binding, onButtonClick)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}