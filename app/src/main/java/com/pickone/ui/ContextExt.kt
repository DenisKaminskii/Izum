package com.pickone.ui

import android.content.Context

fun Context.dpF(dp: Int): Float = dp * resources.displayMetrics.density
fun Context.dp(dp: Int): Int = dpF(dp).toInt()