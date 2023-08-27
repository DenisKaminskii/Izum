package com.izum.ui.packs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.izum.data.Pack
import com.izum.databinding.ItemPackBinding
import com.izum.ui.BaseViewHolder

data class PacksItem(
    val title: String,
    @ColorInt val gradientStartColor: Int,
    @ColorInt val gradientEndColor: Int,
    @ColorInt val contentColor: Int,
    val pollsCount: Long,
    val isPaid: Boolean,
    val hasSubscription: Boolean,
    val pack: Pack
)

class PacksAdapter(
    val onClick: (Pack) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    private val items = mutableListOf<PacksItem>()

    fun setItems(items: List<PacksItem>) {
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
        when(holder) {
            is PackViewHolder -> holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            0 -> {
                val binding = ItemPackBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                PackViewHolder(binding) { pack ->
                    onClick(pack)
                }
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}