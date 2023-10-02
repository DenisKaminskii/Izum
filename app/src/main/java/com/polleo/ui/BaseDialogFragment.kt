package com.polleo.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.polleo.di.IoDispatcher
import com.polleo.ui.route.Router
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import javax.inject.Inject

abstract class BaseDialogFragment : DialogFragment(), CoroutineScope by MainScope() {

    protected val onBackPressedDispatcher by lazy { requireActivity().onBackPressedDispatcher }

    private val router: Router
        get() = (requireActivity() as BaseActivity).router

    @IoDispatcher
    @Inject
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(args = arguments ?: Bundle.EMPTY)
        initSubs()
    }

    open fun initView(args: Bundle) {}
    open fun initSubs() {}
}