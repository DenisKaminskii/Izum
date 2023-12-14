package com.polleo.ui.pack

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
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
import com.polleo.R
import com.polleo.data.Pack
import com.polleo.data.repository.UserRepository
import com.polleo.databinding.DialogPackBinding
import com.polleo.domain.core.PreferenceCache
import com.polleo.ui.BaseDialogFragment
import com.polleo.ui.KEY_ARGS_PACK
import com.polleo.ui.dpF
import com.polleo.ui.packs.PacksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackDialog : BaseDialogFragment() {

    companion object {

        fun show(
            fragmentManager: FragmentManager,
            pack: Pack
        ) {
            PackDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ARGS_PACK, pack)
                }
            }.show(fragmentManager, "PackFragment")
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

    private lateinit var pack: Pack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
        arguments?.let {
            pack = it.getParcelable(KEY_ARGS_PACK)!!
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
                gradientStart = pack.gradientStartColor,
                gradientEnd = pack.gradientEndColor
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
            viewModel.onNewCustomPackStartClick(pack)
            dismissAllowingStateLoss()
        }

        binding.tvSubscribe.setOnClickListener { viewModel.onSubscribeClick() }
        binding.ivHistory.setOnClickListener { viewModel.onPackHistoryClick(pack) }

        binding.tvPolls.text = "${pack.pollsCount} polls"
        binding.tvTitle.text = pack.title

        binding.tvTitle.setTextColor(pack.contentColor)
        binding.ivHistory.setColorFilter(pack.contentColor)
        binding.ivFirst.setColorFilter(pack.contentColor)
        binding.ivSecond.setColorFilter(pack.contentColor)
        binding.ivThird.setColorFilter(pack.contentColor)
        binding.tvPolls.setTextColor(pack.contentColor)
        binding.tvStart.setTextColor(pack.contentColor)
        binding.tvStart.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(pack.contentColor)
        )
        binding.tvSubscribe.setTextColor(pack.contentColor)
        binding.tvSubscribe.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(pack.contentColor)
        )
        binding.tvSubscribe.backgroundTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(pack.contentColor)
        )

        binding.tvStart.isVisible = !pack.isPaid || userRepository.hasSubscription
        binding.tvSubscribe.isVisible = pack.isPaid && !userRepository.hasSubscription

        binding.tvStart.background = GradientDrawable().apply {
            cornerRadius = requireContext().dpF(14)
            setStroke(requireContext().dpF(2).toInt(), pack.contentColor)
        }

        preferenceCache.putLong("${pack.id}_count", pack.pollsCount)
    }

    override fun initSubs() {
        super.initSubs()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { viewState ->  }
            }
        }
    }

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