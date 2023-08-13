package com.izum.ui.pack.polls

import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.data.Pack
import com.izum.data.Poll
import com.izum.data.repository.PacksRepository
import com.izum.databinding.ActivityPackPollsBinding
import com.izum.di.IoDispatcher
import com.izum.ui.BaseActivity
import com.izum.ui.ViewAction
import com.izum.ui.poll.statistic.PollStatisticAdapter
import com.izum.ui.poll.statistic.StatisticItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

// 1. Adapter -> onItem OnClick
@AndroidEntryPoint
class PackPollsActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_PACK = "KEY_ARGS_PACK"
    }

    @Inject
    lateinit var packsRepository: PacksRepository

    private var _binding: ActivityPackPollsBinding? = null
    private val binding: ActivityPackPollsBinding
        get() = _binding!!

    private val adapter = PollStatisticAdapter()

    private var polls = mutableListOf<Poll>()
    private lateinit var pack: Pack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPackPollsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            pack = intent.getParcelableExtra(KEY_ARGS_PACK) ?: return
        } catch (ex: Exception) {
            Log.d("Steve", "PackPollsActivity: onCreate: ${ex.message}")
            finish()
        }
        initView()
        lifecycleScope.launch {
            fetchPolls()
        }
    }

    private fun initView() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvPackTitle.text = pack.title
        binding.rvPolls.adapter = adapter
        binding.rvPolls.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        updateView()
    }

    private fun updateView() = lifecycleScope.launch {
        binding.vProgress.isVisible = polls.isEmpty()
        binding.rvPolls.isVisible = polls.isNotEmpty()

        val statisticItems = polls.map { poll ->
            StatisticItem.TwoOptionsBar(
                leftTop = StatisticItem.TwoOptionsBar.Value.Text(poll.options[0].title),
                leftBottom = StatisticItem.TwoOptionsBar.Value.Text(poll.options[0].votesCount.toString()),
                rightTop = StatisticItem.TwoOptionsBar.Value.Text(poll.options[1].title),
                rightBottom = StatisticItem.TwoOptionsBar.Value.Text(poll.options[1].votesCount.toString()),
                barPercent = poll.options[0].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())
            )
        }

        adapter.setItems(statisticItems)
    }

    private suspend fun fetchPolls() = withContext(ioDispatcher) {
        try {
            packsRepository.getPackPolls(pack.id)
                .collect { polls ->
                    this@PackPollsActivity.polls.clear()
                    this@PackPollsActivity.polls.addAll(polls)
                    updateView()
                }
        } catch (ex: Exception) {
            accept(ViewAction.ShowToast("Polls load failed :("))
            Log.d("Steve", "PackPollsActivity: fetchPolls: ${ex.message}")
            finish()
        }

    }

}