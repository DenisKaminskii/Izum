package com.izum.ui.poll.statistic

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.databinding.ActivityPollStatisticBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

sealed class StatisticItem {

    data class Header(
        val title: String
    ) : StatisticItem()

    data class TwoOptionsBar(
        val leftTop: Value? = null,
        val rightTop: Value? = null,
        val leftBottom: Value? = null,
        val rightBottom: Value? = null,
        val barPercent: Int
    ) : StatisticItem() {

        sealed class Value {
            class Text(val text: String) : Value()
            class Double(val text: Pair<String, String>) : Value()
        }

    }


}

// No View Model
@AndroidEntryPoint
class PollStatisticActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_POLL_ID = "KEY_ARGS_POLL_ID"
    }

    private var _binding: ActivityPollStatisticBinding? = null
    private val binding: ActivityPollStatisticBinding
        get() = _binding!!

    private val adapter = PollStatisticAdapter()

    private var pollId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPollStatisticBinding.inflate(layoutInflater)
        val content = binding.root
        setContentView(content)
        pollId = intent.getLongExtra(KEY_ARGS_POLL_ID, -1L)

        if (pollId == -1L) {
            finish()
        }

        initView()

    }

    private fun initView() {
        binding.rvStatistic.adapter = adapter
        binding.rvStatistic.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
        adapter.setItems(
            listOf(
                StatisticItem.TwoOptionsBar(
                    leftTop = null,
                    rightTop = null,
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Text("Это очень длинная история и мне понадобится две карточки чтобы ее рассказать"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Text("Так вот, дело было вечеромm делать было нечего, так что писал изюм, спс за внмне"),
                    barPercent = 5
                ),
                StatisticItem.Header("Gender"),
                StatisticItem.TwoOptionsBar(
                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("Male"),
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("748" to "57%"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("457" to "43%"),
                    barPercent = 57
                ),
                StatisticItem.TwoOptionsBar(
                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("Female"),
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("1234" to "36%"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("2468" to "64%"),
                    barPercent = 36
                ),
                StatisticItem.TwoOptionsBar(
                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("Other"),
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("0" to "0%"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("100" to "100%"),
                    barPercent = 0
                ),
                StatisticItem.Header("Age"),
                StatisticItem.TwoOptionsBar(
                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("<18"),
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("748" to "57%"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("457" to "43%"),
                    barPercent = 57
                ),
                StatisticItem.TwoOptionsBar(
                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("18-25"),
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("1234" to "36%"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("2468" to "64%"),
                    barPercent = 36
                ),
                StatisticItem.TwoOptionsBar(
                    leftTop = StatisticItem.TwoOptionsBar.Value.Text("25+"),
                    leftBottom = StatisticItem.TwoOptionsBar.Value.Double("984" to "23%"),
                    rightBottom = StatisticItem.TwoOptionsBar.Value.Double("3280" to "77%"),
                    barPercent = 23
                ),
            )
        )
    }


//    private suspend fun getStatistic(pollId: Long) : List<StatisticItem> = withContext(ioDispatcher) {
//
//    }

}