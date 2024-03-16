package com.pickone.ui.packs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.pickone.data.Pack
import com.pickone.databinding.ItemPackBinding
import com.pickone.databinding.ItemPackHeaderBinding
import com.pickone.ui.BaseViewHolder

sealed class PacksItem {

    data class Pack(
        val title: String,
        val description: String,
        @ColorInt val gradientStartColor: Int,
        @ColorInt val gradientEndColor: Int,
        @ColorInt val contentColor: Int,
        val pollsCount: Long,
        val isPaid: Boolean,
        val hasSubscription: Boolean,
        val pack: com.pickone.data.Pack,
        val isMine: Boolean
    ) : PacksItem()

    data class Header(
        val title: String
    ) : PacksItem()

}

class PacksAdapter(
    val onClick: (Pack) -> Unit = {}
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

    enum class ViewType {
        HEADER,
        PACK
    }

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
        return when(items[position]) {
            is PacksItem.Pack -> ViewType.PACK.ordinal
            is PacksItem.Header -> ViewType.HEADER.ordinal
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when(holder) {
            is PackViewHolder -> holder.bind(item as PacksItem.Pack)
            is PackHeaderViewHolder -> holder.bind(item as PacksItem.Header)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            ViewType.PACK.ordinal -> {
                val binding = ItemPackBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                PackViewHolder(binding) { pack ->
                    onClick(pack)
                }
            }
            ViewType.HEADER.ordinal -> {
                val binding = ItemPackHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                PackHeaderViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: BaseViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}