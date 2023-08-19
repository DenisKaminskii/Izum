package com.izum.ui.pack

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.izum.databinding.ItemPackPreviewTwoOptionsBinding
import com.izum.ui.BaseViewHolder

data class PackPreviewItem(
    val topText: String,
    val bottomText: String,
    @ColorInt val textColor: Int
)

class PackPreviewAdapter : RecyclerView.Adapter<BaseViewHolder<*>>() {

    private val items = mutableListOf<PackPreviewItem>()

    fun setItems(items: List<PackPreviewItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when (holder) {
            is PackPreviewTwoOptionsViewHolder -> holder.bind(item)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            0 -> {
                val binding = ItemPackPreviewTwoOptionsBinding.inflate(inflater, parent, false)
                val viewHolder = PackPreviewTwoOptionsViewHolder(binding)
                viewHolder.itemView.layoutParams.width = parent.measuredWidth
                viewHolder
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}