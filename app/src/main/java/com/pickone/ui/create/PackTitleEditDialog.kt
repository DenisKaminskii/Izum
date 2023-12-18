package com.pickone.ui.create

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.pickone.R
import com.pickone.databinding.DialogPackTitleEditBinding
import com.pickone.ui.BaseDialogFragment
import com.pickone.ui.Keyboard
import com.pickone.ui.SimpleTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PackTitleEditDialog : BaseDialogFragment() {

    companion object {

        private const val KEY_ARGS_TITLE = "KEY_ARGS_TITLE"

        fun getInstance(
            title: String? = null,
            onApply: ((String) -> Unit),
            onCancel: (() -> Unit)
        ) : PackTitleEditDialog {
            val dialog = PackTitleEditDialog()
            dialog.arguments = Bundle().apply {
                putString(KEY_ARGS_TITLE, title)
            }
            dialog.onApply = onApply
            dialog.onCancel = onCancel
            return dialog
        }

    }

    private var _binding: DialogPackTitleEditBinding? = null
    private val binding get() = _binding!!

    private var onApply: ((String) -> Unit)? = null
    private var onCancel: (() -> Unit)? = null

    private var prevTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)
        prevTitle = arguments?.getString(KEY_ARGS_TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPackTitleEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun initView(args: Bundle) {
        super.initView(args)
        launch {
            delay(150)
            Keyboard.showSoftKeyboard(binding.etTitle)
        }

        binding.tvCancel.setOnClickListener {
            onCancel?.invoke()
            dismissAllowingStateLoss()
        }
        binding.tvApply.setOnClickListener {
            onApply?.invoke(binding.etTitle.text.toString())
            dismissAllowingStateLoss()
        }
        binding.etTitle.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEnabled = s.toString().isNotBlank()
                binding.tvApply.isEnabled = isEnabled
                binding.tvApply.alpha = if (isEnabled) 1f else 0.5f
            }
        })

        binding.tvApply.isEnabled = prevTitle?.isNotBlank() == true
        binding.tvApply.alpha = if (prevTitle?.isNotBlank() == true) 1f else 0.5f

        prevTitle?.let {
            binding.etTitle.setText(it)
            binding.etTitle.setSelection(it.length)
        }
    }

}