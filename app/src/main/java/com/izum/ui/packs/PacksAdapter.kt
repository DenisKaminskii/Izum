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
    val isOfficial: Boolean,
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
                // get random color from 4 variant
                val color = if (isOfficial) {
                    when((0..3).random()) {
                        0 -> R.color.peach
                        1 -> R.color.mint
                        2 -> R.color.red
                        3 -> R.color.purple
                        else -> R.color.mint
                    }
                } else R.color.black_soft

                PackViewHolder(binding, color) { pack ->
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