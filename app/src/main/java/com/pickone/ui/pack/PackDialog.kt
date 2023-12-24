package com.pickone.ui.pack

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pickone.R
import com.pickone.data.Pack
import com.pickone.data.repository.UserRepository
import com.pickone.databinding.DialogPackBinding
import com.pickone.domain.core.PreferenceCache
import com.pickone.ui.BaseDialogFragment
import com.pickone.ui.KEY_ARGS_INPUT
import com.pickone.ui.dpF
import com.pickone.ui.packs.PacksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class PackDialogInput(
    val pack: Pack,
    val answeredPollsCount: Long
) : Parcelable

@AndroidEntryPoint
class PackDialog : BaseDialogFragment() {

    companion object {

        fun show(
            fragmentManager: FragmentManager,
            input: PackDialogInput
        ) {
            PackDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ARGS_INPUT, input)
                }
            }.show(fragmentManager, "PackDialog")
        }
    }

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var preferenceCache: PreferenceCache

    private var _binding: DialogPackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PacksViewModel by lazy {
        ViewModelProvider(requireActivity())[PacksViewModel::class.java]
    }

    private lateinit var input: PackDialogInput

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
        arguments?.let {
            input = it.getParcelable(KEY_ARGS_INPUT)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(
            createInsetDrawable(
                context = requireContext(),
                horizontalOffset = requireContext().dpF(48),
                gradientStart = input.pack.gradientStartColor,
                gradientEnd = input.pack.gradientEndColor
            )
        )
        return dialog
    }

    override fun initView(args: Bundle) {
        super.initView(args)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.tvStart.setOnClickListener {
            viewModel.onNewCustomPackStartClick(input.pack)
            dismissAllowingStateLoss()
        }

        binding.tvSubscribe.setOnClickListener { viewModel.onSubscribeClick() }
        binding.ivHistory.setOnClickListener { viewModel.onPackHistoryClick(input.pack) }

        binding.tvPolls.text = if (input.answeredPollsCount > 0) {
            "${input.answeredPollsCount} / ${input.pack.pollsCount} answered"
        } else {
            "${input.pack.pollsCount} questions"
        }
        binding.tvTitle.text = input.pack.title
        binding.tvDescription.text = input.pack.description

        binding.tvTitle.setTextColor(input.pack.contentColor)
        binding.ivHistory.setColorFilter(input.pack.contentColor)
        binding.ivFirst.setColorFilter(input.pack.contentColor)
        binding.ivSecond.setColorFilter(input.pack.contentColor)
        binding.ivThird.setColorFilter(input.pack.contentColor)
        binding.tvPolls.setTextColor(input.pack.contentColor)
        binding.tvStart.setTextColor(input.pack.contentColor)
        binding.tvDescription.setTextColor(input.pack.contentColor)
        binding.tvStart.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(input.pack.contentColor)
        )
        binding.tvSubscribe.setTextColor(input.pack.contentColor)
        binding.tvSubscribe.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(input.pack.contentColor)
        )
        binding.tvSubscribe.backgroundTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(input.pack.contentColor)
        )

        binding.tvStart.isVisible = !input.pack.isPaid || userRepository.hasSubscription
        binding.tvSubscribe.isVisible = input.pack.isPaid && !userRepository.hasSubscription
        binding.tvDescription.isVisible = input.pack.description?.isNotBlank() == true
        binding.tvPolls.isVisible = input.pack.pollsCount > 0

        binding.tvStart.background = GradientDrawable().apply {
            cornerRadius = requireContext().dpF(14)
            setStroke(requireContext().dpF(2).toInt(), input.pack.contentColor)
        }
    }

    override fun initSubs() {
        super.initSubs()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { viewState ->  }
            }
        }
    }

    // duplicated code
    private fun createInsetDrawable(
        context: Context,
        horizontalOffset: Float,
        @ColorInt gradientStart: Int,
        @ColorInt gradientEnd: Int
    ): Drawable {
        val baseDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(gradientStart, gradientEnd)
        ).apply {
            cornerRadius = context.dpF(20)
        }

        return InsetDrawable(baseDrawable, horizontalOffset.toInt(), 0, horizontalOffset.toInt(), 0)
    }

}