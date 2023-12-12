package com.polleo.ui.pack

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.DisplayMetrics
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.polleo.R
import com.polleo.data.Pack
import com.polleo.data.repository.UserRepository
import com.polleo.databinding.DialogPackBinding
import com.polleo.domain.core.PreferenceCache
import com.polleo.ui.BaseDialogFragment
import com.polleo.ui.dpF
import com.polleo.ui.packs.PacksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackDialog : BaseDialogFragment() {

    companion object {

        private const val KEY_ARGS_PACK = "KEY_ARGS_PACK"

        fun show(
            fragmentManager: FragmentManager,
            publicPack: Pack.Public
        ) {
            PackDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ARGS_PACK, publicPack)
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

    private lateinit var publicPack: Pack.Public

    private val previewAdapter by lazy { PackPreviewAdapter() }

    private val visibleItemPosition: Int?
        get() = (binding.rvPreview.layoutManager as LinearLayoutManager?)?.findFirstVisibleItemPosition()

    private val onPreviewScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    visibleItemPosition?.let { position ->
                        val checked = R.drawable.ic_radio_checked_24
                        val unchecked = R.drawable.ic_radio_unchecked_24
                        binding.ivFirst.setImageResource(if (position == 0) checked else unchecked)
                        binding.ivSecond.setImageResource(if (position == 1) checked else unchecked)
                        binding.ivThird.setImageResource(if (position == 2) checked else unchecked)
                    }
                }
            }
        }
    }

    private val linearSmoothScroller: LinearSmoothScroller by lazy {
        object : LinearSmoothScroller(requireContext()) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 100f / displayMetrics.densityDpi
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
        arguments?.let {
            publicPack = it.getParcelable(KEY_ARGS_PACK)!!
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
                horizontalOffset = requireContext().dpF(32),
                gradientStart = publicPack.gradientStartColor,
                gradientEnd = publicPack.gradientEndColor
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
            viewModel.onStartClick(publicPack)
            dismissAllowingStateLoss()
        }

        binding.tvSubscribe.setOnClickListener { viewModel.onSubscribeClick() }
        binding.ivHistory.setOnClickListener { viewModel.onPackHistoryClick(publicPack) }

        binding.tvPolls.text = "${publicPack.pollsCount} polls"
        binding.tvTitle.text = publicPack.title

        binding.tvTitle.setTextColor(publicPack.contentColor)
        binding.ivHistory.setColorFilter(publicPack.contentColor)
        binding.ivFirst.setColorFilter(publicPack.contentColor)
        binding.ivSecond.setColorFilter(publicPack.contentColor)
        binding.ivThird.setColorFilter(publicPack.contentColor)
        binding.tvPolls.setTextColor(publicPack.contentColor)
        binding.tvStart.setTextColor(publicPack.contentColor)
        binding.tvStart.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(publicPack.contentColor)
        )
        binding.tvSubscribe.setTextColor(publicPack.contentColor)
        binding.tvSubscribe.compoundDrawableTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(publicPack.contentColor)
        )
        binding.tvSubscribe.backgroundTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(publicPack.contentColor)
        )

        binding.tvStart.isVisible = !publicPack.isPaid || userRepository.hasSubscription
        binding.tvSubscribe.isVisible = publicPack.isPaid && !userRepository.hasSubscription

        binding.tvStart.background = GradientDrawable().apply {
            cornerRadius = requireContext().dpF(14)
            setStroke(requireContext().dpF(2).toInt(), publicPack.contentColor)
        }

        preferenceCache.putLong("${publicPack.id}_count", publicPack.pollsCount)

        initPreviewList()
    }

    private fun initPreviewList() {
        with(binding.rvPreview) {
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.layoutManager = layoutManager

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)

            adapter = previewAdapter
            addOnScrollListener(onPreviewScrollListener)
            onFlingListener = object : RecyclerView.OnFlingListener() {
                override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                    val centerView = snapHelper.findSnapView(layoutManager) ?: return false
                    val position = layoutManager.getPosition(centerView)
                    linearSmoothScroller.targetPosition = position
                    layoutManager.startSmoothScroll(linearSmoothScroller)
                    return true
                }
            }

            previewAdapter.setItems(publicPack.preview
                .take(3)
                .map {
                    PackPreviewItem(
                        topText = it.option1,
                        bottomText = it.option2,
                        textColor = publicPack.contentColor
                    )
                })

            binding.vgIndicators.isVisible = publicPack.preview.size > 1
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