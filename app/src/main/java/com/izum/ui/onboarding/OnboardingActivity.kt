package com.izum.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.izum.databinding.ActivityOnboardingBinding
import com.izum.ui.BaseActivity
import com.izum.ui.dpF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class OnboardingActivity : BaseActivity() {

    private var _binding: ActivityOnboardingBinding? = null
    private val binding get() = _binding!!

    private val firstTitle = "Over 500+ exciting questions!"
    private val secondTitle = "Explore statistics for each question"

    private var isLockInput = true
    private var index = 0

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        super.initView(args)

        binding.tvContinue.setOnClickListener {
            if (isLockInput) return@setOnClickListener
            onContinueClick()
        }

        animateFirstScreenIntro()
        animateSecondScreen()
    }

    private fun animateFirstScreenIntro() = lifecycleScope.launch {
        binding.ivFirstPacks.animate()
            .translationY(0f)
            .setDuration(500)
            .start()

        delay(500)

        isLockInput = false
    }

    private fun animateSecondScreen() = lifecycleScope.launch {
        val votesPercents = listOf(
            42 to 58,
            52 to 48,
            58 to 42,
            48 to 52
        )

        val barsPercents = listOf(
            Triple(65, 10, 25),
            Triple(59, 13, 28),
            Triple(63, 12, 25),
            Triple(68, 9, 23)
        )

        var index = 0

        while (isActive) {
            val votes = votesPercents[index]
            val bars = barsPercents[index]

            binding.tvSecondBottomSubtitle.text = "${votes.first}%"
            binding.tvSecondTopSubtitle.text = "${votes.second}%"

            binding.lbSecondTop.update(LinearBarState(listOf(bars.first, bars.second, bars.third)))
            binding.lbSecondBottom.update(LinearBarState(listOf(bars.third, bars.first, bars.second)))

            index = (index + 1) % votesPercents.size

            delay(1_000)
        }
    }

    private fun onContinueClick() = lifecycleScope.launch {
        isLockInput = true

        hideScreen(index)
        delay(500)

        index++

        showScreen(index)
        delay(500)

        isLockInput = false
    }

    private fun hideScreen(index: Int) = lifecycleScope.launch {
        when(index) {
            0 -> {
                binding.tvTitle.animate()
                    .translationY(-dpF(500))
                    .setDuration(500)
                    .start()

                binding.ivFirstPolls.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .start()

                binding.ivFirstPacks.animate()
                    .translationY(dpF(500))
                    .setDuration(500)
                    .start()

                delay(500)

                binding.vgFirst.isGone = true
            }
            1 -> {
                binding.tvTitle.animate()
                    .translationY(-dpF(500))
                    .setDuration(500)
                    .start()

                binding.vgSecondGenders.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .start()

                binding.ivSecondStatistic.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .start()

                (vgSecondTop + vgSecondBottom).forEach {view ->
                    view.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .start()
                }

                delay(500)
                binding.vgSecond.isGone = true
            }
        }
    }

    private val vgSecondTop: List<View>
        get() = listOf(
            binding.tvSecondTopTitle,
            binding.tvSecondTopSubtitle,
            binding.lbSecondTop
        )

    private val vgSecondBottom: List<View>
        get() = listOf(
            binding.tvSecondBottomTitle,
            binding.tvSecondBottomSubtitle,
            binding.lbSecondBottom
        )

    private fun showScreen(index: Int) {
        when(index) {
            // Statistic
            1 -> {
                binding.tvTitle.text = secondTitle
                binding.vgSecondGenders.alpha = 0f
                binding.ivSecondStatistic.alpha = 0f

                vgSecondTop.forEach { view -> view.translationX = - dpF(500) }
                vgSecondBottom.forEach { view -> view.translationX = dpF(500) }

                binding.vgSecond.isVisible = true

                binding.tvTitle.animate()
                    .translationY(0f)
                    .setDuration(500)
                    .start()

                binding.vgSecondGenders.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()

                binding.ivSecondStatistic.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()

                (vgSecondTop + vgSecondBottom).forEach { view ->
                    view.animate()
                        .translationX(0f)
                        .setDuration(500)
                        .start()
                }
            }
            // Create own content
            2 -> {
                // §
            }
        }
    }

}