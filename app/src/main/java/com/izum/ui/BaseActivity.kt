package com.izum.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.di.IoDispatcher
import com.izum.domain.core.StateViewModel
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.function.Consumer
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : FragmentActivity(), Consumer<ViewAction> {

    @Inject
    lateinit var router: Router

    @IoDispatcher
    @Inject
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router.attachHost(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
    }

    override fun accept(action: ViewAction) {
        when (action) {
            is ViewAction.ShowToast -> showToast(action.message)
            is ViewAction.Finish -> finish()
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