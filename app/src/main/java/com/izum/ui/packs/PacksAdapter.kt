package com.izum.ui.packs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.izum.data.packs.Pack
import com.izum.databinding.ViewHolderPackInfoBinding
import javax.inject.Inject

abstract class PackViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    open fun bind(item: T) = Unit

    open fun unbind() = Unit

}

class PacksAdapter(
    val onPackClick: (Pack) -> Unit
) : RecyclerView.Adapter<PackViewHolder<*>>() {

    private val items = mutableListOf<Pack>()

    fun setItems(items: List<Pack>) {
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

    override fun onBindViewHolder(holder: PackViewHolder<*>, position: Int) {
        val item = items[position]
        when(holder) {
            is PackInfoViewHolder -> holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackViewHolder<*> {
        return when(viewType) {
            0 -> {
                val binding = ViewHolderPackInfoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PackInfoViewHolder(binding) { pack ->
                    onPackClick(pack)
                }
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onViewRecycled(holder: PackViewHolder<*>) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

}