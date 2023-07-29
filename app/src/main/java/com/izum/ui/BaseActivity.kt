package com.izum.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.izum.di.IoDispatcher
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import java.util.function.Consumer
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : ComponentActivity(), Consumer<ViewAction> {

    @Inject
    lateinit var router: Router

    @IoDispatcher
    @Inject lateinit var ioDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        router.attachHost(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
    }

    override fun accept(action: ViewAction) {
        when(action) {
            is ViewAction.ShowToast -> showToast(action.message)
            is ViewAction.Finish -> finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}