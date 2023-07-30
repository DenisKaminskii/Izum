package com.izum.ui.create

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.R
import com.izum.databinding.FragmentCreatePollBinding
import com.izum.ui.dp
import com.izum.ui.dpF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePollFragment : Fragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentCreatePollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePollViewModel by lazy {
        ViewModelProvider(requireActivity())[CreatePollViewModel::class.java]
    }

    private val inputCardBackground: GradientDrawable
        get() = GradientDrawable().apply {
            cornerRadius = requireContext().dpF(16)
            setColor(requireContext().getColor(R.color.white))
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePollBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInputCard(binding.vgTop)
        initInputCard(binding.vgBottom)
        binding.root.setOnClickListener {
            when {
                binding.vTop.isFocused -> {
                    hideSoftKeyboard(binding.vTop)
                    binding.vTop.clearFocus()
                }
                binding.vBottom.isFocused -> {
                    hideSoftKeyboard(binding.vBottom)
                    binding.vBottom.clearFocus()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> update(state) }
            }
        }
    }

    private fun update(state: CreatePollViewState) {
        binding.vTop.isEnabled = state is CreatePollViewState.Input
        binding.vBottom.isEnabled = state is CreatePollViewState.Input
    }

    private fun initInputCard(vgOption: ViewGroup) {
        vgOption.background = inputCardBackground
        vgOption.elevation = requireContext().dpF(6)

        val inputView = vgOption.getChildAt(0)
        vgOption.setOnClickListener {
            showSoftKeyboard(inputView)
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

}