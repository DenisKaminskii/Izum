package com.izum.data.repository

import com.izum.data.Poll
import com.izum.data.PollOption

object PacksMocks {
    fun getPackMocks(packId: Long): List<Poll> {
        return listOf(
            // generate 10 mock polls
            Poll(
                id = 1,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 1,
                        title = "Banana",
                        votesCount = 78
                    ),
                    PollOption(
                        id = 2,
                        title = "Strawberry",
                        votesCount = 25
                    )
                )
            ),
            Poll(
                id = 2,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 1,
                        title = "Car",
                        votesCount = 142
                    ),
                    PollOption(
                        id = 2,
                        title = "Plain",
                        votesCount = 90
                    )
                )
            ),
            Poll(
                id = 3,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 1,
                        title = "Cats",
                        votesCount = 532
                    ),
                    PollOption(
                        id = 2,
                        title = "Dogs",
                        votesCount = 252
                    )
                )
            ),
            Poll(
                id = 4,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 1,
                        title = "House",
                        votesCount = 11
                    ),
                    PollOption(
                        id = 2,
                        title = "Girl",
                        votesCount = 6
                    )
                )
            ),
            Poll(
                id = 5,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 1,
                        title = "Cats",
                        votesCount = 532
                    ),
                    PollOption(
                        id = 2,
                        title = "Dogs",
                        votesCount = 252
                    )
                )
            ),
            Poll(
                id = 6,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 3,
                        title = "Birds",
                        votesCount = 123
                    ),
                    PollOption(
                        id = 4,
                        title = "Fish",
                        votesCount = 432
                    )
                )
            ),
            Poll(
                id = 7,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 5,
                        title = "Chocolate",
                        votesCount = 654
                    ),
                    PollOption(
                        id = 6,
                        title = "Vanilla",
                        votesCount = 321
                    )
                )
            ),
            Poll(
                id = 8,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 7,
                        title = "Coffee",
                        votesCount = 421
                    ),
                    PollOption(
                        id = 8,
                        title = "Tea",
                        votesCount = 134
                    )
                ),
            ),
            Poll(
                id = 9,
                packId = packId,
                options = listOf(
                    PollOption(
                        id = 9,
                        title = "Beach",
                        votesCount = 753
                    ),
                    PollOption(
                        id = 10,
                        title = "Mountains",
                        votesCount = 512
                    )
                )
            )
        )
    }
}