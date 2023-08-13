package com.izum.ui

import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseDialogFragment : DialogFragment(), CoroutineScope by MainScope() {

    protected val onBackPressedDispatcher by lazy { requireActivity().onBackPressedDispatcher }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}