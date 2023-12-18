package com.pickone.ui.custom

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.pickone.R
import com.pickone.databinding.DialogAddCustomPackBinding
import com.pickone.ui.BaseDialogFragment
import com.pickone.ui.Keyboard
import com.pickone.ui.SimpleTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddCustomPackDialog : BaseDialogFragment() {

    companion object {

        fun getInstance(
            onStart: ((packId: Long) -> Unit)
        ) : AddCustomPackDialog {
            val dialog = AddCustomPackDialog()
            dialog.onStart = onStart
            return AddCustomPackDialog()
        }

    }

    private val viewModel: AddCustomPackViewModel by viewModels()

    private var _binding: DialogAddCustomPackBinding? = null
    private val binding get() = _binding!!

    private var onStart: ((Long) -> Unit)? = null

    private val animDuration = 225L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddCustomPackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun initView(args: Bundle) {
        super.initView(args)
        launch {
            delay(150)
            Keyboard.showSoftKeyboard(binding.etCode)
        }

        binding.tvCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.tvAdd.setOnClickListener {
            viewModel.onAddCode(binding.etCode.text.toString())
        }
        binding.etCode.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEnabled = s.toString().isNotBlank()
                binding.tvAdd.isEnabled = isEnabled
                binding.tvAdd.alpha = if (isEnabled) 1f else 0.5f

                if (binding.tvError.isVisible) {
                    binding.tvError.animate()
                        .alpha(0f)
                        .setDuration(animDuration)
                        .start()
                }
            }
        })

        viewModel.onViewInitialized(Unit)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { state -> update(state) }
    }

    private fun update(state: AddCustomPackViewState) = with(binding) {
        tvTitle.animate()
            .alpha(
                when(state) {
                    is AddCustomPackViewState.Success -> 0f
                    else -> 1f
                }
            )
            .setDuration(animDuration)
            .start()

        etCode.isEnabled = state is AddCustomPackViewState.Default
                || state is AddCustomPackViewState.InvalidCode
                || state is AddCustomPackViewState.NotFound

        etCode.animate()
            .alpha(
                when(state) {
                    is AddCustomPackViewState.Success -> 0f
                    else -> 1f
                }
            )
            .setDuration(animDuration)
            .start()

        tvCancel.isEnabled = state is AddCustomPackViewState.Default
                || state is AddCustomPackViewState.InvalidCode
                || state is AddCustomPackViewState.NotFound

        tvAdd.isEnabled = state is AddCustomPackViewState.Default && !etCode.text.isNullOrBlank()

        vgButtons.animate()
            .alpha(
                when(state) {
                    is AddCustomPackViewState.Success,
                    AddCustomPackViewState.Loading -> 0f
                    else -> 1f
                }
            )
            .setDuration(animDuration)
            .start()

        tvError.text = when (state) {
            AddCustomPackViewState.NotFound -> "There is no pack with such code"
            else -> "Invalid code"
        }

        tvError.animate()
            .alpha(
                when(state) {
                    AddCustomPackViewState.InvalidCode, AddCustomPackViewState.NotFound -> 1f
                    else -> 0f
                }
            )
            .setDuration(animDuration)
            .start()

        vProgress.animate()
            .alpha(
                when(state) {
                    AddCustomPackViewState.Loading -> 1f
                    else -> 0f
                }
            )
            .setDuration(animDuration)
            .start()

        if (state is AddCustomPackViewState.Success) {
            showCustomPackAdded(state)
            tvAdd.setOnClickListener {
                val packId = state.pack.id
                onStart?.invoke(packId)
                dismissAllowingStateLoss()
            }
        }
    }

    private fun showCustomPackAdded(state: AddCustomPackViewState.Success) = with(binding) {
//        launch {
//            delay(animDuration)
//
//            val startFrom = requireContext().getColor(R.color.black_gradient_start)
//            val endFrom = requireContext().getColor(R.color.black_gradient_end)
//
//            val startTo = requireContext().getColor(R.color.white_gradient_start)
//            val endTo = requireContext().getColor(R.color.white_gradient_end)
//
//            val colorStartAnimation = ValueAnimator.ofObject(ArgbEvaluator(), startFrom, startTo)
//            colorStartAnimation.duration = animDuration * 3
//
//            val colorEndAnimation = ValueAnimator.ofObject(ArgbEvaluator(), endFrom, endTo)
//            colorEndAnimation.duration = animDuration * 3
//
//            var startColor = startFrom
//            var endColor = endFrom
//
//            fun updateBackground() {
//                root.background = GradientDrawable(
//                    GradientDrawable.Orientation.BL_TR,
//                    intArrayOf(startColor, endColor)
//                ).apply {
//                    cornerRadius = requireContext().dpF(14)
//                }
//            }
//
//            colorStartAnimation.addUpdateListener { animator ->
//                startColor = animator.animatedValue as Int
//                updateBackground()
//            }
//
//            colorEndAnimation.addUpdateListener { animator ->
//                endColor = animator.animatedValue as Int
//                updateBackground()
//            }
//
//            colorStartAnimation.start()
//            colorEndAnimation.start()
//        }

        launch {
            delay(animDuration * 2)

            tvTitle.text = state.pack.title
            tvTitle.animate()
                .alpha(1f)
                .setDuration(animDuration * 2)
                .start()

            tvCancel.isVisible = false

            tvAdd.text = "Start"
            tvAdd.isEnabled = true

            vgButtons.animate()
                .alpha(1f)
                .setDuration(animDuration * 2)
                .start()

            tvPolls.text = state.pack.pollsCount.toString()
            tvPolls.animate()
                .alpha(1f)
                .setDuration(animDuration * 2)
                .start()
        }
    }


}

