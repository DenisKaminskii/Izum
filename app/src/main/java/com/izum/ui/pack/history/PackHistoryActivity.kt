package com.izum.ui.pack.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.data.Pack
import com.izum.data.Poll
import com.izum.data.repository.PacksRepository
import com.izum.databinding.ActivityPackHistoryBinding
import com.izum.ui.BaseActivity
import com.izum.ui.poll.statistic.PollStatisticAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PackHistoryActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_PACK = "KEY_ARGS_PACK"
    }

    @Inject
    lateinit var packsRepository: PacksRepository

    private var _binding: ActivityPackHistoryBinding? = null
    private val binding: ActivityPackHistoryBinding
        get() = _binding!!

    private val adapter = PollStatisticAdapter {
        viewModel.onPollClick()
    }

    private lateinit var pack: Pack

    private val viewModel: PackHistoryViewModel by viewModels()

    override fun initArgs(args: Bundle) {
        super.initArgs(args)
        pack = args.getParcelable(KEY_ARGS_PACK)!!
    }

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPackHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvNoPolls.setOnClickListener { viewModel.onNoPollsClicked() }

        binding.tvPackTitle.text = pack.title
        binding.rvPolls.adapter = adapter
        binding.rvPolls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        update(PackHistoryViewState.Loading)

        viewModel.onViewInitialized(PackHistoryViewModel.Companion.Arguments(pack))
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(viewState: PackHistoryViewState) {
        binding.vProgress.isVisible = viewState is PackHistoryViewState.Loading
        binding.vgError.isVisible = viewState is PackHistoryViewState.Error
        binding.vgNoPolls.isVisible = viewState is PackHistoryViewState.Empty

        if (viewState !is PackHistoryViewState.Content) return
        binding.rvPolls.isVisible = viewState.polls.isNotEmpty()
        adapter.setItems(viewState.polls)
    }

}