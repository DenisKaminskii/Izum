package com.izum.ui.create.pack

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.slider.Slider
import com.izum.R
import com.izum.databinding.ActivityCreatePackBinding
import com.izum.ui.BaseActivity
import com.izum.ui.SimpleTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePackActivity : BaseActivity() {

    private var _binding: ActivityCreatePackBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CreatePackViewModel by viewModels()
    private var onSliderChangeListener: Slider.OnChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreatePackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        update(viewModel.viewState)
        
        lifecycleScope.launch { 
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> update(state) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewActionsFlow.collect { action -> accept(action) }
            }
        }

        viewModel.init(Unit)
    }

    private val titleTextWatcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.onTitleTextChanged(s.toString())
        }
    }

    private fun initView() {
        binding.vBack.setOnClickListener { viewModel.onBackClick() }
        binding.vDone.setOnClickListener { viewModel.onDoneClick() }
        binding.vTitle.addTextChangedListener(titleTextWatcher)

        binding.vSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                viewModel.onSliderTrackingStart()
            }

            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.onSliderTrackingStop()
            }
        })
    }

    private fun update(state: CreatePackViewState) {
        binding.vDone.isVisible = state is CreatePackViewState.Input
        binding.vLoading.isVisible = state is CreatePackViewState.Loading

        when (state) {
            is CreatePackViewState.Loading -> {
                // nothing
            }

            is CreatePackViewState.Input -> {
                binding.vDone.isEnabled = state.isDoneEnabled
                binding.vSlider.isVisible = state.slider != null
                binding.vDone.imageTintList = ContextCompat.getColorStateList(
                    this,
                    if (state.isDoneEnabled) R.color.black else R.color.disabledIcon
                )
                binding.vCounter.text = "${state.visibleIndex}/${state.visibleLastIndex}"

                binding.vPrev.apply(state.prevButton, R.drawable.ic_arrow_back_24) { viewModel.onPrevClick(state.prevButton) }
                binding.vNext.apply(state.nextButton, R.drawable.ic_arrow_forward_24) { viewModel.onNextClick(state.nextButton) }

                if (onSliderChangeListener == null && state.slider != null) {
                    onSliderChangeListener = Slider.OnChangeListener { slider, value, fromUser ->
                        if (!fromUser) return@OnChangeListener

                        val lastAvailableIndex = state.slider.lastAvailableIndex
                        if (lastAvailableIndex != null && value > lastAvailableIndex) {
                            binding.vSlider.value = lastAvailableIndex.toFloat()
                        } else {
                            viewModel.onSliderChanged(value.toInt())
                        }

                    }
                    binding.vSlider.valueFrom = 0f
                    binding.vSlider.addOnChangeListener(onSliderChangeListener!!)
                }

                state.slider?.let {
                    binding.vSlider.valueTo = it.lastIndex.toFloat()
                    binding.vSlider.value = it.index.toFloat()
                }
            }
        }
    }

    private fun ImageView.apply(
        state: CreatePackViewState.Button,
        arrowResId: Int,
        onClick: View.OnClickListener
    ) {
        when(state) {
            CreatePackViewState.Button.HIDDEN -> {
                isVisible = false
            }
            CreatePackViewState.Button.ENABLED -> {
                isVisible = true
                isEnabled = true
                setImageResource(arrowResId)
                imageTintList = ContextCompat.getColorStateList(
                    this@CreatePackActivity,
                    R.color.black
                )
            }
            CreatePackViewState.Button.DISABLED -> {
                isVisible = true
                isEnabled = false
                setImageResource(arrowResId)
                imageTintList = ContextCompat.getColorStateList(
                    this@CreatePackActivity,
                    R.color.disabledIcon
                )
            }
            CreatePackViewState.Button.CREATE_NEW -> {
                isVisible = true
                isEnabled = true
                setImageResource(R.drawable.ic_add_24)
                imageTintList = ContextCompat.getColorStateList(
                    this@CreatePackActivity,
                    R.color.black
                )
            }
        }

        setOnClickListener(onClick)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.vTitle.removeTextChangedListener(titleTextWatcher)
    }
}