package com.pickone.ui.paywall

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.pickone.databinding.ItemPricePremiumBinding
import com.pickone.ui.dp
import com.pickone.ui.dpF

data class PremiumPriceItemState(
    val title: String,
    val subtitle: String,
    val isSelected: Boolean,
    @ColorInt val titleColor: Int,
    @ColorInt val subtitleColor: Int,
    @DrawableRes val icon: Int,
    @ColorInt val accentColor: Int
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

        tvTitle.setTextColor(state.titleColor)
        tvSubtitle.setTextColor(state.subtitleColor)
        ivCheck.setImageResource(state.icon)

        root.background = GradientDrawable().apply {
            setColor(
                if (state.isSelected) state.accentColor
                else context.getColor(android.R.color.transparent)
            )
            if (!state.isSelected) {
                setStroke(
                    context.dp(2),
                    state.accentColor
                )
            }
            cornerRadius = context.dpF(14)
        }
    }

}