package com.izum.ui.packs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.izum.R
import com.izum.data.Pack
import com.izum.databinding.ViewHolderPackBinding
import com.izum.ui.BaseViewHolder

class PacksAdapter(
    val onPackClick: (Pack) -> Unit
) : RecyclerView.Adapter<BaseViewHolder<*>>() {

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

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]
        when(holder) {
            is PackViewHolder -> holder.bind(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType) {
            0 -> {
                val binding = ViewHolderPackBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PackViewHolder(binding, R.color.purple) { pack ->
                    onPackClick(pack)
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