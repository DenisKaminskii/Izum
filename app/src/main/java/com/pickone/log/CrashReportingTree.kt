package com.pickone.log

import android.util.Log
import timber.log.Timber
import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashReportingTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logMessage = buildLogMessage(priority, tag, message)

        if (t != null) {
            FirebaseCrashlytics.getInstance().let {
                it.log(logMessage)
                it.recordException(t)
            }
        } else {
            // Firebase.analytics.logEvent(logMessage, null)
        }
    }

    private fun buildLogMessage(
        priority: Int,
        tag: String?,
        message: String,
    ) = buildString {
        append(priority.priorityAsString())
        append(": ")
        if (tag != null) {
            append("[")
            append(tag)
            append("]: ")
        }
        append(message)
    }

    private fun Int.priorityAsString() = when (this) {
        Log.ERROR -> "ERROR"
        Log.WARN -> "WARN"
        Log.ASSERT -> "WTF"
        else -> error("other priorities are not loggable")
    }
}