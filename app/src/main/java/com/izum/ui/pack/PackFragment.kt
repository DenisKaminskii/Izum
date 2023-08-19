package com.izum.ui.pack

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.izum.R
import com.izum.data.Pack
import com.izum.data.PackColors
import com.izum.data.repository.UserRepository
import com.izum.databinding.DialogFragmentPackBinding
import com.izum.ui.BaseDialogFragment
import com.izum.ui.dpF
import com.izum.ui.packs.PacksViewModel
import com.izum.ui.packs.PacksViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class PackFragment : BaseDialogFragment() {

    companion object {

        private const val KEY_ARGS_PACK = "KEY_ARGS_PACK"

        fun show(
            fragmentManager: FragmentManager,
            pack: Pack
        ) {
            PackFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ARGS_PACK, pack)
                }
            }.show(fragmentManager, "PackFragment")
        }
    }

    @Inject
    lateinit var userRepository: UserRepository

    private var _binding: DialogFragmentPackBinding? = null
    private val binding: DialogFragmentPackBinding
        get() = _binding!!

    private val viewModel: PacksViewModel by lazy {
        ViewModelProvider(requireActivity())[PacksViewModel::class.java]
    }

    private lateinit var pack: Pack

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
    }

    override fun initArgs(args: Bundle) {
        super.initArgs(args)
        pack = args.getParcelable(KEY_ARGS_PACK)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentPackBinding.inflate(inflater, container, false)
        return binding.root
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
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(
            createInsetDrawable(
                context = requireContext(),
                horizontalOffset = requireContext().dpF(32),
                colors = pack.colors
            )
        )
        return dialog
    }

    override fun initView() {
        super.initView()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.tvStart.setOnClickListener { viewModel.onStartClick(pack) }
        binding.tvSubscribe.setOnClickListener { /* */ }
        binding.ivHistory.setOnClickListener { viewModel.onPackHistoryClick(pack) }

        binding.tvPolls.text = "${pack.pollsCount} polls"
        binding.tvTitle.text = pack.title

        binding.tvTitle.setTextColor(pack.colors.contentColor)
        binding.ivHistory.setColorFilter(pack.colors.contentColor)
        binding.ivFirst.setColorFilter(pack.colors.contentColor)
        binding.ivSecond.setColorFilter(pack.colors.contentColor)
        binding.ivThird.setColorFilter(pack.colors.contentColor)
        binding.tvPolls.setTextColor(pack.colors.contentColor)
        binding.tvStart.setTextColor(pack.colors.contentColor)

        binding.tvStart.isVisible = !pack.isPaid || userRepository.hasSubscription
        binding.tvSubscribe.isVisible = pack.isPaid && !userRepository.hasSubscription

        binding.tvStart.background = GradientDrawable().apply {
            cornerRadius = requireContext().dpF(14)
            setStroke(requireContext().dpF(2).toInt(), pack.colors.contentColor)
        }


        initPreviewList()
    }

    override fun initSubs() {
        super.initSubs()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { viewState -> update(viewState) }
            }
        }
    }

    private fun update(viewState: PacksViewState) {
        when (viewState) {
            is PacksViewState.Loading -> {}
            is PacksViewState.Packs -> {
                previewAdapter.setItems(
                    listOf(
                        PackPreviewItem(
                            topText = "Что-то типо того",
                            bottomText = "Или что-то типо этого",
                            textColor = pack.colors.contentColor
                        ),
                        PackPreviewItem(
                            topText = "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                            bottomText = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout.",
                            textColor = pack.colors.contentColor
                        ),
                        PackPreviewItem(
                            topText = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable.",
                            bottomText = "Latin words, combined with a handful of model sentence structures, to generate Lorem Ipsum which looks reasonable",
                            textColor = pack.colors.contentColor
                        )
                    )
                )

                binding.vgIndicators.isVisible = visibleItemPosition != null
            }
        }
    }

    private fun createInsetDrawable(
        context: Context,
        horizontalOffset: Float,
        colors: PackColors
    ): Drawable {
        val baseDrawable = GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            intArrayOf(colors.gradientStartColor, colors.gradientEndColor)
        ).apply {
            cornerRadius = context.dpF(20)
        }

        return InsetDrawable(baseDrawable, horizontalOffset.toInt(), 0, horizontalOffset.toInt(), 0)
    }

}