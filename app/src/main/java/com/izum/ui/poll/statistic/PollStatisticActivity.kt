package com.izum.ui.poll.statistic

import android.os.Bundle
import android.util.Log
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

// No View Model
@AndroidEntryPoint
class PollStatisticActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_POLL = "KEY_ARGS_POLL"
    }

    private var _binding: ActivityPollStatisticBinding? = null
    private val binding: ActivityPollStatisticBinding
        get() = _binding!!

    private val adapter = PollStatisticAdapter { /* ignore */ }

    private val pollOptions: StatisticItem
        get() = StatisticItem.TwoOptionsBar(
            leftTop = null,
            rightTop = null,
            leftBottom = StatisticItem.TwoOptionsBar.Value(
                text = poll.options[0].title,
                color = getColor(R.color.red)
            ),
            rightBottom = StatisticItem.TwoOptionsBar.Value(
                text = poll.options[1].title,
                 color = getColor(R.color.sand)
            ),
            barPercent = poll.options[0].votesCount.toInt() * 100 / (poll.options[0].votesCount.toInt() + poll.options[1].votesCount.toInt())
        )

    private val statistic = mutableListOf<StatisticItem>()

    private lateinit var poll: Poll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            fetchStatistic()
        }
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
        binding.rvStatistic.adapter = adapter
        binding.rvStatistic.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        updateView()
    }

    private fun updateView() = lifecycleScope.launch {
        binding.vProgress.isVisible = statistic.isEmpty()
        binding.rvStatistic.isVisible = statistic.isNotEmpty()
        adapter.setItems(
            listOf(pollOptions) + statistic
        )
    }

    private suspend fun fetchStatistic() = withContext(ioDispatcher) {
        delay(1_000)
//        statistic.addAll(
//            listOf(
//                StatisticItem.Header("Gender"),
//                StatisticItem.TwoOptionsBar(
//                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("Male"),
//                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("748" to "57%"),
//                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("457" to "43%"),
//                    barPercent = 57
//                ),
//                StatisticItem.TwoOptionsBar(
//                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("Female"),
//                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("1234" to "36%"),
//                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("2468" to "64%"),
//                    barPercent = 36
//                ),
//                StatisticItem.TwoOptionsBar(
//                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("Other"),
//                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("0" to "0%"),
//                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("100" to "100%"),
//                    barPercent = 0
//                ),
//                StatisticItem.Header("Age"),
//                StatisticItem.TwoOptionsBar(
//                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("<18"),
//                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("748" to "57%"),
//                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("457" to "43%"),
//                    barPercent = 57
//                ),
//                StatisticItem.TwoOptionsBar(
//                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("18-25"),
//                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("1234" to "36%"),
//                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("2468" to "64%"),
//                    barPercent = 36
//                ),
//                StatisticItem.TwoOptionsBar(
//                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("25+"),
//                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("984" to "23%"),
//                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("3280" to "77%"),
//                    barPercent = 23
//                ),
//            )
//        )
        updateView()
    }

}