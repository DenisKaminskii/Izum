package com.pickone.log

import timber.log.Timber

internal class PickoneUncaughtExceptionHandler(
    private val threadDefaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread, e: Throwable) {
        Timber.tag("Thread.UncaughtExceptionHandler").e(e, "Uncaught Exception detected in thread $t")
        threadDefaultUncaughtExceptionHandler?.uncaughtException(t, e)
    }
}