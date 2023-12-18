package com.pickone.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.route.Router
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.function.Consumer
import javax.inject.Inject

abstract class BaseDialogFragment : DialogFragment(),
    Consumer<ViewAction>,
    CoroutineScope by MainScope() {

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

    override fun accept(action: ViewAction) {
        when (action) {
            is ViewAction.ShowToast -> showToast(action.message)
            else -> {}
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun <T> subscribe(viewModel: StateViewModel<*, T>, onViewState: (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> onViewState(state) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewActionsFlow.collect { action -> accept(action) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routeFlow.collect { route -> router.route(route) }
            }
        }
    }

    open fun initView(args: Bundle) {}
    open fun initSubs() {}
}