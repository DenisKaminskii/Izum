package com.polleo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.polleo.di.IoDispatcher
import com.polleo.domain.core.StateViewModel
import com.polleo.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.function.Consumer
import javax.inject.Inject

const val KEY_ARGS_INPUT = "KEY_ARGS_INPUT"
const val KEY_ARGS_PACK = "KEY_ARGS_PACK"

@AndroidEntryPoint
abstract class BaseActivity : FragmentActivity(), Consumer<ViewAction> {

    @Inject
    lateinit var router: Router

    @IoDispatcher
    @Inject
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        initSubs()
        initView(args = intent.extras ?: Bundle.EMPTY)
    }

    open fun initLayout() {}
    open fun initSubs() {}
    @Throws(Exception::class)
    open fun initView(args: Bundle) {}

    override fun onStart() {
        super.onStart()
        router.attachHost(this)
    }

    override fun onStop() {
        super.onStop()
        router.detachHost()
    }

    override fun accept(action: ViewAction) {
        when (action) {
            is ViewAction.ShowToast -> showToast(action.message)
            else -> {}
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

}