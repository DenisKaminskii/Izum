package com.izum.ui.poll.statistic

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.izum.R
import com.izum.databinding.ItemStatisticTwoOptionsBinding
import com.izum.ui.BaseViewHolder
import com.izum.ui.dpF
import kotlin.math.max

class StatisticTwoOptionsViewHolder(
    private val binding: ItemStatisticTwoOptionsBinding,
    val onClick: () -> Unit
) : BaseViewHolder<StatisticItem.TwoOptionsBar>(binding.root) {

    override fun bind(item: StatisticItem.TwoOptionsBar) {
        super.bind(item)
        val context = binding.root.context

        binding.vLeft.background = GradientDrawable().apply {
            setColor(context.getColor(R.color.red))
            cornerRadius = context.dpF(4)
        }

        binding.vRight.background = GradientDrawable().apply {
            setColor(context.getColor(R.color.blue))
            cornerRadius = context.dpF(4)
        }

        binding.vMiddle.setGuidelinePercent(
            max(0.05f, item.barPercent.toFloat() / 100)
        )

        setValue(binding.tvLeftTop, item.leftTop)
        setValue(binding.tvRightTop, item.rightTop)
        setValue(binding.tvLeftBottom, item.leftBottom)
        setValue(binding.tvRightBottom, item.rightBottom)

        binding.root.setOnClickListener { onClick() }
    }


    private fun setValue(
        textView: TextView,
        value: StatisticItem.TwoOptionsBar.Value?
    ) {
        textView.isVisible = value != null
        if (value == null) return

        textView.setTextColor(value.color)
        textView.text = value.text
    }

    override fun unbind() {
        super.unbind()
        binding.root.setOnClickListener(null)
    }
}