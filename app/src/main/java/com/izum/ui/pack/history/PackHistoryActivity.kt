package com.izum.ui.pack.history

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.R
import com.izum.data.Pack
import com.izum.data.repository.PublicPacksRepository
import com.izum.databinding.ActivityPackHistoryBinding
import com.izum.ui.BaseActivity
import com.izum.ui.KEY_ARGS_INPUT
import com.izum.ui.poll.list.PollsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class PackHistoryInput(
    val packId: Long,
    val packTitle: String
) : Parcelable

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
        val input = args.getParcelable<PackHistoryInput>(KEY_ARGS_INPUT)!!

        binding.ivBack.setOnClickListener { finish() }
        binding.tvNoPolls.setOnClickListener { viewModel.onNoPollsClicked() }
        binding.ivFormat.setOnClickListener { viewModel.onFormatClicked() }

        binding.tvPackTitle.text = input.packTitle
        binding.rvPolls.adapter = adapter
        binding.rvPolls.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        viewModel.onViewInitialized(input)
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
        binding.ivFormat.setImageResource(
            if (viewState.isValueInNumbers) R.drawable.ic_numbers_24
            else R.drawable.ic_percent_24
        )
        adapter.setItems(viewState.polls)
    }

}