package com.izum.ui.pack.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.data.Pack
import com.izum.data.repository.PublicPacksRepository
import com.izum.databinding.ActivityPackHistoryBinding
import com.izum.ui.BaseActivity
import com.izum.ui.poll.list.PollsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PackHistoryActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_PACK = "KEY_ARGS_PACK"
    }

    @Inject
    lateinit var publicPacksRepository: PublicPacksRepository

    private var _binding: ActivityPackHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PackHistoryViewModel by viewModels()

    private val adapter = PollsAdapter(
        onStatisticClick = { viewModel.onPollClick() }
    )

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPackHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        val publicPack: Pack.Public = args.getParcelable(KEY_ARGS_PACK)!!

        binding.ivBack.setOnClickListener { finish() }
        binding.tvNoPolls.setOnClickListener { viewModel.onNoPollsClicked() }

        binding.tvPackTitle.text = publicPack.title
        binding.rvPolls.adapter = adapter
        binding.rvPolls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.onViewInitialized(PackHistoryViewModel.Args(publicPack))
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