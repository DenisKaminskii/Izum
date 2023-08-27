package com.izum.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.databinding.FragmentEditPollBinding
import com.izum.ui.SimpleTextWatcher
import com.izum.ui.dpF
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPollFragment : Fragment(), CoroutineScope by MainScope() {

    private var _binding: FragmentEditPollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPollViewModel by lazy {
        ViewModelProvider(requireActivity())[EditPollViewModel::class.java]
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
        _binding = FragmentEditPollBinding.inflate(inflater, container, false)
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
        binding.vBottom.addTextChangedListener(bottomTextWatcher)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> update(state) }
            }
        }
    }

    private fun update(state: EditPollViewState) {
        binding.vTop.isEnabled = state is EditPollViewState.Input
        binding.vBottom.isEnabled = state is EditPollViewState.Input
    }

    private fun initInputCard(vgOption: ViewGroup) {
        // vgOption.background = inputCardBackground
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

    override fun onDestroy() {
        super.onDestroy()
        binding.root.setOnClickListener(null)
        binding.vTop.removeTextChangedListener(topTextWatcher)
        binding.vBottom.removeTextChangedListener(bottomTextWatcher)
    }

}