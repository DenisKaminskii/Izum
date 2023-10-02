package com.polleo.ui.paywall

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import com.polleo.databinding.ItemPricePremiumBinding
import com.polleo.ui.dp
import com.polleo.ui.dpF

data class PremiumPriceItemState(
    val title: String,
    val price: String,
    val subtitle: String,
    val isSelected: Boolean,
    @ColorInt
    val primaryColor: Int,
    @ColorInt
    val textColor: Int
)

class PremiumPriceItemView : LinearLayout {

    constructor(context: Context) : super(context) {
        init(null, 0)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        _binding = ItemPricePremiumBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private var _binding: ItemPricePremiumBinding? = null
    private val binding get() = _binding!!

    fun update(state: PremiumPriceItemState) = with(binding) {
        tvTitle.text = state.title
        tvSubtitle.text = state.subtitle
        tvPrice.text = state.price

        root.background = GradientDrawable().apply {
            setColor(
                if (state.isSelected) state.primaryColor
                else context.getColor(android.R.color.transparent)
            )
            if (!state.isSelected) {
                setStroke(
                    context.dp(2),
                    state.primaryColor
                )
            }
            cornerRadius = context.dpF(24)
        }

        tvTitle.setTextColor(if (state.isSelected) state.textColor else state.primaryColor)
        tvSubtitle.setTextColor(if (state.isSelected) state.textColor else state.primaryColor)
        tvPrice.setTextColor(if (state.isSelected) state.textColor else state.primaryColor)
    }

}