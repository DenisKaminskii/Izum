package com.izum.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.izum.databinding.ActivityMainBinding
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var router: Router

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val content = binding.root
        setContentView(content)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                router.attachHost(this@MainActivity)
                viewModel.routeFlow.collect {
                    router.route(it)
                }
            }
        }

        viewModel.init(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
    }



}