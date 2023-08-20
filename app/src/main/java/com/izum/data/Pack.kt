package com.izum.data

import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize

// 2. Сделать PackHistoryActivity -> пустой экран (вы еще ничего не прошли, веперд отвечать!)
// 3. Надо как-то юзеру прокоммуницировать чтобы ждал новых опросов после того как прошел пак
// 4. Предложить опрос
// 5. Создать пак опросов
@Parcelize
data class Pack(
    val id: Long,
    val title: String,
    val description: String?,
    val isPaid: Boolean,
    val productId: String?,
    val pollsCount: Long,
    val authorId: Long?,
    @ColorInt val contentColor: Int,
    @ColorInt val gradientStartColor: Int,
    @ColorInt val gradientEndColor: Int,
    val preview: List<PackPreview>
) : Parcelable

@Parcelize
data class PackPreview(
    val option1: String,
    val option2: String
) : Parcelable