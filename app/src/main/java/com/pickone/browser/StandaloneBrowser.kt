package com.pickone.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.pickone.R

object StandaloneBrowser {

    fun intent(context: Context, url: String): Intent {
        val customTabsIntent = customTabsIntent(context)
        return customTabsIntent.intent.apply {
            data = Uri.parse(url)
        }
    }

    fun open(context: Context, url: String) {
        try {
            val customTabsIntent = customTabsIntent(context)
            customTabsIntent.launchUrl(context, Uri.parse(url))
        } catch (ex: Throwable) {
            Toast.makeText(context, "Browser open failed \uD83D\uDE1F\nPlease, visit our Play Store page.", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    private fun customTabsIntent(context: Context): CustomTabsIntent =
        CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.white))
            .build()
}