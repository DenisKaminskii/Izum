package com.izum.ui.create.pack

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.R
import com.izum.databinding.ActivityCreatePackBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePackActivity : BaseActivity() {

    private var _binding: ActivityCreatePackBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CreatePackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreatePackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.vBack.setOnClickListener { viewModel.onBackClick() }
        binding.vDone.setOnClickListener { viewModel.onDoneClick() }
        
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

        update(viewModel.viewState)
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
                val slider = state.slider
                binding.vCounter.isInvisible = slider == null
                if (slider != null) {
                    binding.vCounter.text = "${slider.index}/${slider.lastIndex}"
                }

                // TODO: вынести в метод
                when(state.prevButton) {
                    CreatePackViewState.Button.HIDDEN -> {
                        binding.vPrev.isVisible = false
                    }
                    CreatePackViewState.Button.ENABLED -> {
                        binding.vPrev.isVisible = true
                        binding.vPrev.isEnabled = true
                        binding.vPrev.setImageResource(R.drawable.ic_arrow_back_24)
                        binding.vPrev.imageTintList = ContextCompat.getColorStateList(
                            this,
                            R.color.black
                        )
                    }
                    CreatePackViewState.Button.DISABLED -> {
                        binding.vPrev.isVisible = true
                        binding.vPrev.isEnabled = false
                        binding.vPrev.setImageResource(R.drawable.ic_arrow_back_24)
                        binding.vPrev.imageTintList = ContextCompat.getColorStateList(
                            this,
                            R.color.disabledIcon
                        )
                    }
                    CreatePackViewState.Button.CREATE_NEW -> {
                        binding.vPrev.isVisible = true
                        binding.vPrev.isEnabled = true
                        binding.vPrev.setImageResource(R.drawable.ic_add_24)
                        binding.vPrev.imageTintList = ContextCompat.getColorStateList(
                            this,
                            R.color.black
                        )
                    }
                }
                when(state.nextButton) {
                    CreatePackViewState.Button.HIDDEN -> {
                        binding.vNext.isVisible = false
                    }
                    CreatePackViewState.Button.ENABLED -> {
                        binding.vNext.isVisible = true
                        binding.vNext.isEnabled = true
                        binding.vNext.setImageResource(R.drawable.ic_arrow_forward_24)
                        binding.vNext.imageTintList = ContextCompat.getColorStateList(
                            this,
                            R.color.black
                        )
                    }
                    CreatePackViewState.Button.DISABLED -> {
                        binding.vNext.isVisible = true
                        binding.vNext.isEnabled = false
                        binding.vNext.setImageResource(R.drawable.ic_arrow_forward_24)
                        binding.vNext.imageTintList = ContextCompat.getColorStateList(
                            this,
                            R.color.disabledIcon
                        )
                    }
                    CreatePackViewState.Button.CREATE_NEW -> {
                        binding.vNext.isVisible = true
                        binding.vNext.isEnabled = true
                        binding.vNext.setImageResource(R.drawable.ic_add_24)
                        binding.vNext.imageTintList = ContextCompat.getColorStateList(
                            this,
                            R.color.black
                        )
                        binding.vNext.setOnClickListener {
                            viewModel.onCreateNewClick()
                        }
                    }
                }
            }
        }
    }
}