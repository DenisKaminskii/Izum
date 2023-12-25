package com.pickone.ui.route

import android.content.Intent
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.pickone.data.Pack
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
import com.pickone.ui.BaseActivity
import com.pickone.ui.KEY_ARGS_INPUT
import com.pickone.ui.KEY_ARGS_PACK
import com.pickone.ui.create.EditPackActivity
import com.pickone.ui.create.EditPackInput
import com.pickone.ui.edit.EditPollActivity
import com.pickone.ui.edit.EditPollVariant
import com.pickone.ui.onboarding.OnboardingActivity
import com.pickone.ui.pack.PackDialog
import com.pickone.ui.pack.PackDialogInput
import com.pickone.ui.pack.history.PackHistoryActivity
import com.pickone.ui.packs.PacksActivity
import com.pickone.ui.paywall.PaywallActivity
import com.pickone.ui.paywall.PaywallInput
import com.pickone.ui.poll.PollActivity
import com.pickone.ui.poll.statistic.PollStatisticActivity
import com.pickone.ui.poll.statistic.PollStatisticInput
import com.pickone.ui.user.UserInfoActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

interface Router {

    sealed interface Route {
        object Packs : Route
        data class Polls(val pack: com.pickone.data.Pack) : Route
        data class EditPoll(val variant: EditPollVariant) : Route
        data class EditPack(val input: EditPackInput) : Route
        data class Statistic(val input: PollStatisticInput) : Route
        data class Pack(val input: PackDialogInput) : Route
        data class PackHistory(val pack: com.pickone.data.Pack) : Route
        object ProvideUserInfo : Route
        object Finish : Route
        data class Paywall(val input: PaywallInput = PaywallInput(fromOnboarding = false)) : Route
        object Onboarding : Route
        data class GoogleReview(val reviewInfo: ReviewInfo) : Route
    }

    fun route(route: Route)

    fun attachHost(activity: BaseActivity)

    fun detachHost()

}

class RouterImpl @Inject constructor(
    private val preferenceCache: PreferenceCache
) : Router, CoroutineScope by MainScope() {

    private val routeQueue = ConcurrentLinkedQueue<Router.Route>()

    private var host: BaseActivity? = null

    override fun attachHost(activity: BaseActivity) {
        this.host = activity
        executeRoutes()
    }

    override fun detachHost() {
        host = null
    }

    override fun route(route: Router.Route) {
        routeQueue.add(route)
        executeRoutes()
    }

    private fun executeRoutes() {
        host ?: return

        while(routeQueue.isNotEmpty()) {
            routeQueue.poll()?.let { route ->
                when(route) {
                    Router.Route.Packs -> showPacks()
                    is Router.Route.Polls -> showPackPolls(route.pack)
                    is Router.Route.EditPoll -> showEditPoll(route.variant)
                    is Router.Route.EditPack -> showEditPack(route.input)
                    is Router.Route.Statistic -> showPollStatistic(route.input)
                    is Router.Route.Pack -> showPackDialog(route.input)
                    is Router.Route.PackHistory -> showPackHistory(route.pack)
                    is Router.Route.ProvideUserInfo -> showUserInfo()
                    is Router.Route.Finish -> finish()
                    is Router.Route.Paywall -> showSubscriptionPaywall(route.input)
                    is Router.Route.Onboarding -> showOnboarding()
                    is Router.Route.GoogleReview -> showGoogleReview(route.reviewInfo)
                }
            }
        }
    }

    private fun showPacks() {
        host?.let { activity ->
            val intent = Intent(activity, PacksActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    private fun showPackPolls(pack: Pack) {
        host?.let { activity ->
            val intent = Intent(activity, PollActivity::class.java)
            intent.putExtra(KEY_ARGS_PACK, pack)
            activity.startActivity(intent)
        }
    }

    private fun showEditPoll(variant: EditPollVariant) {
        host?.let { activity ->
            val intent = Intent(activity, EditPollActivity::class.java)
            intent.putExtra(EditPollActivity.KEY_ARGS_EDIT_POLL_VARIANT, variant)
            activity.startActivity(intent)
        }
    }

    private fun showEditPack(input: EditPackInput) {
        host?.let { activity ->
            val intent = Intent(activity, EditPackActivity::class.java)
            intent.putExtra(KEY_ARGS_INPUT, input)
            activity.startActivity(intent)
        }
    }

    private fun showPollStatistic(input: PollStatisticInput) {
        host?.let { activity ->
            val intent = Intent(activity, PollStatisticActivity::class.java)
            intent.putExtra(KEY_ARGS_INPUT, input)
            activity.startActivity(intent)
        }
    }

    private fun showPackDialog(input: PackDialogInput) {
        host?.let { activity ->
            PackDialog.show(activity.supportFragmentManager, input)
        }
    }

    private fun showPackHistory(pack: Pack) {
        host?.let { activity ->
            val intent = Intent(activity, PackHistoryActivity::class.java)
            intent.putExtra(KEY_ARGS_PACK, pack)
            activity.startActivity(intent)
        }
    }

    private fun showUserInfo() {
        host?.let { activity ->
            val intent = Intent(activity, UserInfoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    private fun showSubscriptionPaywall(input: PaywallInput) {
        host?.let { activity ->
            val intent = Intent(activity, PaywallActivity::class.java)
            intent.putExtra(KEY_ARGS_INPUT, input)
            activity.startActivity(intent)
        }
    }

    private fun showOnboarding() {
        host?.let { activity ->
            val intent = Intent(activity, OnboardingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private fun showGoogleReview(reviewInfo: ReviewInfo) {
        host?.let { activity ->
            val manager = ReviewManagerFactory.create(activity)
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener { _ ->
                preferenceCache.putBoolean(PreferenceKey.GoogleStoreReviewShown.name, true)
            }
        }
    }

    private fun finish() {
        host?.finish()
    }

}