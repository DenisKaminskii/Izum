package com.izum.ui.poll.statistic

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.databinding.ActivityPollStatisticBinding
import com.izum.ui.BaseActivity
import com.izum.ui.poll.list.PollsAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PollStatisticActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_POLL_ID = "KEY_ARGS_POLL_ID"
    }

    private var _binding: ActivityPollStatisticBinding? = null
    private val binding: ActivityPollStatisticBinding
        get() = _binding!!

    private val viewModel: PollStatisticViewModel by viewModels()

    private val adapter = PollsAdapter(
        onStatisticClick = { viewModel.onStatisticClick() }
    )

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPollStatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvRetry.setOnClickListener { viewModel.onRetryClick() }
        binding.tvShare.setOnClickListener { viewModel.onShareClick() }

        binding.rvStatistic.adapter = adapter
        binding.rvStatistic.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.onViewInitialized(
            input = intent.getLongExtra(KEY_ARGS_POLL_ID, -1L)
        )
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel, ::update)
    }

    private fun update(state: PollStatisticViewState) {
        binding.vProgress.isVisible = state is PollStatisticViewState.Loading
        binding.rvStatistic.isVisible = state is PollStatisticViewState.Stats || state is PollStatisticViewState.NoData
        binding.vgError.isVisible = state is PollStatisticViewState.Error
        binding.vgNoData.isVisible = state is PollStatisticViewState.NoData

        when(state) {
            is PollStatisticViewState.NoData -> {
                adapter.setItems(listOf(state.options))
            }
            is PollStatisticViewState.Stats -> {
                adapter.setItems(state.stats)
            }
            else -> {}
        }
    }

}