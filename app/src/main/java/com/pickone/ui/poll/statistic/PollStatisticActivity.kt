package com.pickone.ui.poll.statistic

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.pickone.R
import com.pickone.databinding.ActivityPollStatisticBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.KEY_ARGS_INPUT
import com.pickone.ui.poll.list.PollsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

@Parcelize
data class PollStatisticInput(
    val pollId: Long,
    val votedOptionId: Long? = null,
    val isCustomPack: Boolean = false,
    val shareLink: String? = null,
) : Parcelable

@AndroidEntryPoint
class PollStatisticActivity : BaseActivity() {

    private var _binding: ActivityPollStatisticBinding? = null
    private val binding: ActivityPollStatisticBinding
        get() = _binding!!

    private val viewModel: PollStatisticViewModel by viewModels()

    private val adapter = PollsAdapter()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPollStatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvRetry.setOnClickListener { viewModel.onRetryClick() }
        binding.tvShare.setOnClickListener { viewModel.onShareClick(
            clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
        ) }
        binding.tvSubscribe.setOnClickListener { viewModel.onSubscribeClick() }
        binding.ivFormat.setOnClickListener { viewModel.onFormatClicked() }

        binding.rvStatistic.adapter = adapter
        binding.rvStatistic.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.onViewInitialized(
            input = args.getParcelable(KEY_ARGS_INPUT)!!
        )
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel, ::update)
    }

    private fun update(state: PollStatisticViewState) {
        binding.vProgress.isVisible = state is PollStatisticViewState.Loading
        binding.rvStatistic.isVisible = state is PollStatisticViewState.Stats
                || state is PollStatisticViewState.NoData
                || state is PollStatisticViewState.NoSubscription

        binding.vgError.isVisible = state is PollStatisticViewState.Error
        binding.ivFormat.isVisible = state is PollStatisticViewState.Stats
        binding.vgNoData.isVisible = state is PollStatisticViewState.NoData
        binding.vgSubscribe.isVisible = state is PollStatisticViewState.NoSubscription

        when(state) {
            is PollStatisticViewState.NoData -> {
                adapter.setItems(listOf(state.options))
            }
            is PollStatisticViewState.Stats -> {
                adapter.setItems(state.stats)
                binding.ivFormat.setImageResource(
                    if (state.isValueInNumbers) R.drawable.ic_numbers_24
                    else R.drawable.ic_percent_24
                )
            }
            is PollStatisticViewState.NoSubscription -> {
                adapter.setItems(listOf(state.options))
            }
            else -> {}
        }
    }

}