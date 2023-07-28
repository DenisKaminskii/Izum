package com.izum.ui.poll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.izum.databinding.ActivityPollBinding
import com.izum.ui.route.Router
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PollActivity : ComponentActivity() {

    companion object {
        const val KEY_ARGS_PACK_ID = "KEY_ARGS_PACK_ID"
    }

    @Inject lateinit var router: Router

    private var _binding: ActivityPollBinding? = null
    private val binding: ActivityPollBinding
        get() = _binding!!

    private val viewModel: PollViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPollBinding.inflate(layoutInflater)
        val content = binding.root
        setContentView(content)

        router.attachHost(this@PollActivity)

        lifecycleScope.launch {
            viewModel.uiStateFlow.collect { state ->
                state
                    .ifLoading {

                    }
                    .ifPack { pack ->

                    }
            }
        }

        viewModel.init(
            PollViewModel.Companion.Arguments(
                packId = intent.getLongExtra(KEY_ARGS_PACK_ID, -1)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        router.detachHost()
    }

}