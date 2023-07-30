package com.izum.ui.create.pack

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.R
import com.izum.databinding.FragmentCreatePackPollBinding
import com.izum.ui.SimpleTextWatcher
import com.izum.ui.dpF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatePackPollFragment : Fragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentCreatePackPollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePackViewModel by lazy {
        ViewModelProvider(requireActivity())[CreatePackViewModel::class.java]
    }

    private val inputCardBackground: GradientDrawable
        get() = GradientDrawable().apply {
            cornerRadius = requireContext().dpF(16)
            setColor(requireContext().getColor(R.color.white))
        }

    private val topTextWatcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.onTopTextChanged(s.toString())
        }
    }

    private val bottomTextWatcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.onBottomTextChanged(s.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePackPollBinding.inflate(inflater, container, false)
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

        binding.vTop.addTextChangedListener(topTextWatcher)
        binding.vTop.removeTextChangedListener(bottomTextWatcher)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> update(state) }
            }
        }
    }

    private fun update(state: CreatePackViewState) {
        binding.vTop.isEnabled = state is CreatePackViewState.Input
        binding.vBottom.isEnabled = state is CreatePackViewState.Input

        when (state) {
            is CreatePackViewState.Input -> {
                val newTopText = state.topText
                val oldTopText = binding.vTop.text.toString()
                if (newTopText != oldTopText) {
                    binding.vTop.setText(newTopText)
                    binding.vTop.setSelection(newTopText.length)
                }

                val newBottomText = state.bottomText
                val oldBottomText = binding.vBottom.text.toString()
                if (newBottomText != oldBottomText) {
                    binding.vBottom.setText(newBottomText)
                    binding.vBottom.setSelection(newBottomText.length)
                }
            }
            is CreatePackViewState.Loading -> {
                // nothing
            }
        }
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
            val imm =
                ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.root.setOnClickListener(null)
        binding.vTop.removeTextChangedListener(topTextWatcher)
        binding.vBottom.removeTextChangedListener(bottomTextWatcher)
    }

}