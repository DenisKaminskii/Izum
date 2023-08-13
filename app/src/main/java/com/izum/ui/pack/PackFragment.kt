package com.izum.ui.pack

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.izum.R
import com.izum.data.Pack
import com.izum.databinding.DialogFragmentPackBinding
import com.izum.ui.BaseDialogFragment
import com.izum.ui.dpF
import com.izum.ui.packs.PacksViewModel
import com.izum.ui.packs.PacksViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)

        try {
            pack = requireArguments().getParcelable(KEY_ARGS_PACK)!!
        } catch (e: Exception) {
            Log.d("Steve", "PackFragment onCreate: pack is null. Exception: $e")
            dismiss()
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentPackBinding.inflate(inflater, container, false)

        with(binding.rvPreview) {

            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.layoutManager = layoutManager

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(this)

            adapter = previewAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            visibleItemPosition?.let { position ->
                                binding.ivFirst.setImageResource(
                                    if (position == 0) R.drawable.ic_radio_checked_24
                                    else R.drawable.ic_radio_unchecked_24
                                )
                                binding.ivSecond.setImageResource(
                                    if (position == 1) R.drawable.ic_radio_checked_24
                                    else R.drawable.ic_radio_unchecked_24
                                )
                                binding.ivThird.setImageResource(
                                    if (position == 2) R.drawable.ic_radio_checked_24
                                    else R.drawable.ic_radio_unchecked_24
                                )
                            }
                        }
                    }
                }
            })
        }

        binding.tvPolls.text = "${pack.pollsCount} polls"

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(
            createInsetDrawable(
                color = requireContext().getColor(R.color.purple),
                cornerRadius = requireContext().dpF(24),
                leftInset = requireContext().dpF(16),
                rightInset = requireContext().dpF(16)
            )
        )
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

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
                with(binding.tvAction) {
                    val hasSubscription = viewState.hasSubscription

                    text = if (hasSubscription) "Join" else "Subscribe"
                    setTextColor(
                        requireContext().getColor(
                            if (hasSubscription) R.color.black_wet else R.color.white
                        )
                    )
                    setCompoundDrawables(
                        null,
                        null,
                        if (hasSubscription) requireContext().getDrawable(R.drawable.ic_unlock_24) else null,
                        null
                    )
                    setBackgroundResource(
                        if (hasSubscription) R.drawable.rect_filled_sand_14dp else R.drawable.rect_filled_gradient_premium_14dp
                    )
                }

                previewAdapter.setItems(
                    listOf(
                        PackPreviewItem(
                            topText = "Что-то типо того",
                            bottomText = "Или что-то типо этого"
                        ),
                        PackPreviewItem(
                            topText = "It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
                            bottomText = "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout."
                        ),
                        PackPreviewItem(
                            topText = "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable.",
                            bottomText = "Latin words, combined with a handful of model sentence structures, to generate Lorem Ipsum which looks reasonable"
                        )
                    )
                )

                binding.vgIndicators.isVisible = visibleItemPosition != null
            }
        }
    }

    private fun updateView() {
        binding.vgIndicators.isVisible = visibleItemPosition != null

        visibleItemPosition?.let { position ->
            binding.ivFirst.setImageResource(
                if (position == 0) R.drawable.ic_radio_checked_24
                else R.drawable.ic_radio_unchecked_24
            )
            binding.ivSecond.setImageResource(
                if (position == 1) R.drawable.ic_radio_checked_24
                else R.drawable.ic_radio_unchecked_24
            )
            binding.ivThird.setImageResource(
                if (position == 2) R.drawable.ic_radio_checked_24
                else R.drawable.ic_radio_unchecked_24
            )
        }
    }

    private fun createInsetDrawable(
        color: Int,
        cornerRadius: Float,
        leftInset: Float,
        rightInset: Float
    ): Drawable {
        val baseDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            this.cornerRadius = cornerRadius
        }

        return InsetDrawable(baseDrawable, leftInset.toInt(), 0, rightInset.toInt(), 0)
    }

}