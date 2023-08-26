package com.izum.ui.poll.statistic

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.WorkerThread
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.R
import com.izum.data.Poll
import com.izum.databinding.ActivityPollStatisticBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PollStatisticActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_POLL = "KEY_ARGS_POLL"
    }

    private var _binding: ActivityPollStatisticBinding? = null
    private val binding: ActivityPollStatisticBinding
        get() = _binding!!

    private val viewModel: PollStatisticViewModel by viewModels()
    private lateinit var poll: Poll

    private val adapter = PollStatisticAdapter {
        viewModel.onStatisticClick()
    }

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPollStatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initArgs(args: Bundle) {
        super.initArgs(args)
        poll = intent.getParcelableExtra(KEY_ARGS_POLL)!!
    }

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvRetry.setOnClickListener { viewModel.onRetryClick() }

        binding.rvStatistic.adapter = adapter
        binding.rvStatistic.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.onViewInitialized(poll)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel, ::update)
    }

    private fun update(state: PollStatisticViewState) {
        binding.vProgress.isVisible = state is PollStatisticViewState.Loading
        binding.rvStatistic.isVisible = state is PollStatisticViewState.Stats
        binding.vgError.isVisible = state is PollStatisticViewState.Error

        if (state !is PollStatisticViewState.Stats) return
        adapter.setItems(state.stats)
    }

}