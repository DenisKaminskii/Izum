package com.pickone.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.pickone.data.DeviceIdProvider
import com.pickone.ui.paywall.Price
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface Analytics {

    fun onboardingTapOnPurchase(price: Price)
    fun onboardingPurchaseSucceeded(price: Price)
    fun onboardingPurchaseCancelled()
    fun onboardingPurchaseError()
    fun onboardingClosed()
    fun profileSetupConfigured()
    fun profileSetupExitedApp()
    fun paywall0pen()
    fun paywallTapOnPurchase(price: Price)
    fun paywallPurchaseSucceeded(price: Price)
    fun paywallPurchaseCancelled()
    fun paywallPurchaseError()
    fun paywallClosed()
    fun packStartTap(packId: Long, packName: String)
    fun packStatsTap()
    fun packPreviewClose(packId: Long, packName: String)
    fun pollVoted(pollId: Long, voteId: Long)
    fun pollExited(pollId: Long)
    fun customPackStartTap()
    fun customPackStatsTap()
    fun customPackPreviewClose(packId: Long, packName: String)
    fun customPollVoted(pollId: Long, voteId: Long)
    fun customPackCreateTap()
    fun customPackCreated()
    fun customPackCreateError()
    fun customPackAddPollTap()
    fun customPackAddedPoll(pollsCount: Int)
    fun customPackAddPollError()
    fun customPackRemovedPoll()
    fun customPackShareTap()

}

class FirebaseAnalytics @Inject constructor(
    @ApplicationContext context: Context,
    deviceIdProvider: DeviceIdProvider
) : Analytics {

    private val firebaseAnalytics: FirebaseAnalytics

    init {
        FirebaseApp.initializeApp(context)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.setUserProperty("device_id", deviceIdProvider.deviceId)
    }

    private fun logEvent(name: String, params: Map<String, String>? = null) {
        firebaseAnalytics.logEvent(name, Bundle().apply {
            if (params == null) return@apply
            params.forEach { (key, value) -> putString(key, value) }
        })
    }

    override fun onboardingTapOnPurchase(price: Price) {
        logEvent("onboardingTapOnPurchase", null)
    }

    override fun onboardingPurchaseSucceeded(price: Price) {
        logEvent(
            name = "onboardingPurchaseSucceeded",
            params = mapOf("price" to price.name)
        )
    }

    override fun onboardingPurchaseCancelled() {
        logEvent("onboardingPurchaseCancelled", null)
    }

    override fun onboardingPurchaseError() {
        logEvent("onboardingPurchaseError", null)
    }

    override fun onboardingClosed() {
        logEvent("onboardingClosed", null)
    }

    override fun profileSetupConfigured() {
        logEvent("profileSetupConfigured", null)
    }

    override fun profileSetupExitedApp() {
        logEvent("profileSetupExitedApp", null)
    }

    override fun paywall0pen() {
        logEvent("paywall0pen", null)
    }

    override fun paywallTapOnPurchase(price: Price) {
        logEvent(
            name = "paywallTapOnPurchase",
            params = null
        )
    }

    override fun paywallPurchaseSucceeded(price: Price) {
        logEvent("paywallPurchaseSucceeded", null)
    }

    override fun paywallPurchaseCancelled() {
        logEvent("paywallPurchaseCancelled", null)
    }

    override fun paywallPurchaseError() {
        logEvent("paywallPurchaseError", null)
    }

    override fun paywallClosed() {
        logEvent("paywallClosed", null)
    }

    override fun packStartTap(packId: Long, packName: String) {
        logEvent(
            name = "packStartTap",
            params = mapOf(
                "packId" to packId.toString(),
                "packName" to packName
            )
        )
    }

    override fun packStatsTap() {
        logEvent(name = "packStatsTap")
    }

    override fun packPreviewClose(packId: Long, packName: String) {
        logEvent(
            name = "packPreviewClose",
            params = mapOf(
                "packId" to packId.toString(),
                "packName" to packName
            )
        )
    }

    override fun pollVoted(pollId: Long, voteId: Long) {
        logEvent(
            name = "pollVoted",
            params = mapOf(
                "pollId" to pollId.toString(),
                "voteId" to voteId.toString()
            )
        )
    }

    override fun pollExited(pollId: Long) {
        logEvent(
            name = "pollExited",
            params = mapOf(
                "pollId" to pollId.toString()
            )
        )
    }

    override fun customPackStartTap() {
        logEvent("customPackStartTap", null)
    }

    override fun customPackStatsTap() {
        logEvent("customPackStatsTap", null)
    }

    override fun customPackPreviewClose(packId: Long, packName: String) {
        logEvent(
            name = "customPackPreviewClose",
            params = mapOf(
                "packId" to packId.toString(),
                "packName" to packName
            )
        )
    }

    override fun customPollVoted(pollId: Long, voteId: Long) {
        logEvent(
            name = "customPollVoted",
            params = mapOf(
                "pollId" to pollId.toString(),
                "voteId" to voteId.toString()
            )
        )
    }

    override fun customPackCreateTap() {
        logEvent("customPackCreateTap", null)
    }

    override fun customPackCreated() {
        logEvent("customPackCreated", null)
    }

    override fun customPackCreateError() {
        logEvent("customPackCreateError", null)
    }

    override fun customPackAddPollTap() {
        logEvent("customPackAddPollTap", null)
    }

    override fun customPackAddedPoll(pollsCount: Int) {
        logEvent(
            name = "customPackAddedPoll",
            params = mapOf(
                "pollsCount" to pollsCount.toString()
            )
        )
    }

    override fun customPackAddPollError() {
        logEvent("customPackAddPollError", null)
    }

    override fun customPackRemovedPoll() {
        logEvent("customPackRemovedPoll", null)
    }

    override fun customPackShareTap() {
        logEvent("customPackShareTap", null)
    }

}