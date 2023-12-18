package com.pickone.data

data class PollStatistic(
    val options: List<PollOption>,
    val sections: List<PollStatisticSection>
)

data class PollStatisticSection(
    val title: String,
    val categories: List<PollStatisticCategory>
)

data class PollStatisticCategory(
    val title: String,
    val options: List<PollOption>
)