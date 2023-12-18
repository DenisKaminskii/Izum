package com.pickone.ui.pack.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.pickone.R
import com.pickone.data.Pack
import com.pickone.data.repository.PublicPacksRepository
import com.pickone.databinding.ActivityPackHistoryBinding
import com.pickone.ui.BaseActivity
import com.pickone.ui.KEY_ARGS_PACK
import com.pickone.ui.poll.list.PollsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PackHistoryActivity : BaseActivity() {

    @Inject
    lateinit var publicPacksRepository: PublicPacksRepository

    private var _binding: ActivityPackHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PackHistoryViewModel by viewModels()

    private val adapter = PollsAdapter(
        onStatisticClick = { pollId -> viewModel.onPollClick(pollId) }
    )

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPackHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        val pack = args.getParcelable<Pack>(KEY_ARGS_PACK)!!

        binding.ivBack.setOnClickListener { finish() }
        binding.tvStart.setOnClickListener { viewModel.onStartClicked() }
        binding.tvSubscribe.setOnClickListener { viewModel.onSubscribeAndStartClicked() }
        binding.ivFormat.setOnClickListener { viewModel.onFormatClicked() }

        binding.tvPackTitle.text = pack.title
        binding.rvPolls.adapter = adapter
        binding.rvPolls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.onViewInitialized(pack)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(viewState: PackHistoryViewState) {
        binding.vProgress.isVisible = viewState is PackHistoryViewState.Loading
        binding.vgError.isVisible = viewState is PackHistoryViewState.Error
        binding.vgNoPolls.isVisible = viewState is PackHistoryViewState.Empty
        binding.tvStart.isVisible = viewState is PackHistoryViewState.Empty && !viewState.isPaid
        binding.tvSubscribe.isVisible = viewState is PackHistoryViewState.Empty && viewState.isPaid
        binding.ivFormat.isVisible = viewState is PackHistoryViewState.Content

        if (viewState !is PackHistoryViewState.Content) return
        binding.rvPolls.isVisible = viewState.polls.isNotEmpty()
        binding.ivFormat.setImageResource(
            if (viewState.isValueInNumbers) R.drawable.ic_numbers_24
            else R.drawable.ic_percent_24
        )
        adapter.setItems(viewState.polls)
    }

}