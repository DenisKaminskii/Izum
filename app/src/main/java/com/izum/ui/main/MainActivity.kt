package com.izum.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import com.izum.databinding.ActivityMainBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {


    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        val content = binding.root
        setContentView(content)

        subscribe(viewModel) { /* no viewState */ }

        viewModel.onViewInitialized(Unit)
    }

}