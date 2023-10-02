package com.polleo.ui.onboarding

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.polleo.R
import kotlin.math.max

data class LinearBarState(
    val percents: List<Int>
)

class LinearBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bars = listOf(
        Bar(ContextCompat.getColor(context, R.color.gender_male)),
        Bar(ContextCompat.getColor(context, R.color.gender_female)),
        Bar(ContextCompat.getColor(context, R.color.gender_other))
    )
    private val minBarWidth = context.resources.getDimensionPixelSize(R.dimen.min_bar_width)
    private val barMargin = context.resources.getDimensionPixelSize(R.dimen.bar_margin)
    private val barRadius = context.resources.getDimensionPixelSize(R.dimen.bar_radius)

    init {
        update(LinearBarState(listOf(33, 33, 34)))
    }

    override fun onDraw(canvas: Canvas) {
        var left = 0
        for (bar in bars) {
            val right = left + bar.width
            canvas.drawRoundRect(
                left.toFloat(), 0f, right.toFloat(), height.toFloat(),
                barRadius.toFloat(), barRadius.toFloat(), bar.paint
            )
            left = right + barMargin
        }
    }

    fun update(state: LinearBarState) {
        val totalWidth = width - (bars.size - 1) * barMargin
        state.percents.forEachIndexed { index, percent ->
            val targetWidth = max(minBarWidth, totalWidth * percent / 100)
            val animator = ValueAnimator.ofInt(bars[index].width, targetWidth)
            animator.addUpdateListener {
                bars[index].width = it.animatedValue as Int
                invalidate()
            }
            animator.duration = 350
            animator.start()
        }
    }

    private class Bar(val color: Int, var width: Int = 0) {
        val paint = Paint().apply {
            this.color = this@Bar.color
        }
    }
}
