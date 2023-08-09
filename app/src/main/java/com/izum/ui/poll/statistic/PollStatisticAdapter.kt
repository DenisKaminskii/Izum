package com.izum.ui.poll.statistic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.izum.databinding.ItemStatisticHeaderBinding
import com.izum.databinding.ItemStatisticTwoOptionsBinding
import com.izum.ui.BaseViewHolder

class PollStatisticAdapter : RecyclerView.Adapter<BaseViewHolder<*>>() {

    enum class ViewType {
        HEADER,
        TWO_OPTIONS
    }

    private val items = mutableListOf<StatisticItem>()

    fun setItems(items: List<StatisticItem>) {
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
            is StatisticItem.Header -> ViewType.HEADER.ordinal
            is StatisticItem.TwoOptionsBar -> ViewType.TWO_OPTIONS.ordinal
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when(holder) {
            is StatisticHeaderViewHolder -> holder.bind(item as StatisticItem.Header)
            is StatisticTwoOptionsViewHolder -> holder.bind(item as StatisticItem.TwoOptionsBar)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.HEADER.ordinal -> {
                val binding = ItemStatisticHeaderBinding.inflate(inflater, parent, false)
                StatisticHeaderViewHolder(binding)
            }
            ViewType.TWO_OPTIONS.ordinal -> {
                val binding = ItemStatisticTwoOptionsBinding.inflate(inflater, parent, false)
                StatisticTwoOptionsViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}