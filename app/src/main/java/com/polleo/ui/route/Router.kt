package com.polleo.ui.route

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.polleo.data.Pack
import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.PreferenceKey
import com.polleo.ui.KEY_ARGS_INPUT
import com.polleo.ui.KEY_ARGS_PACK
import com.polleo.ui.onboarding.OnboardingActivity
import com.polleo.ui.create.EditPackActivity
import com.polleo.ui.create.EditPackInput
import com.polleo.ui.edit.EditPollActivity
import com.polleo.ui.edit.EditPollVariant
import com.polleo.ui.pack.PackDialog
import com.polleo.ui.pack.history.PackHistoryActivity
import com.polleo.ui.packs.PacksActivity
import com.polleo.ui.paywall.SubscriptionPaywallActivity
import com.polleo.ui.poll.PollActivity
import com.polleo.ui.poll.statistic.PollStatisticActivity
import com.polleo.ui.poll.statistic.PollStatisticInput
import com.polleo.ui.user.UserInfoActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

interface Router {

    sealed interface Route {
        object Packs : Route
        data class Polls(val pack: com.polleo.data.Pack) : Route
        data class EditPoll(val variant: EditPollVariant) : Route
        data class EditPack(val input: EditPackInput) : Route
        data class Statistic(val input: PollStatisticInput) : Route
        data class Pack(val pack: com.polleo.data.Pack) : Route
        data class PackHistory(val pack: com.polleo.data.Pack) : Route
        object ProvideUserInfo : Route
        object Finish : Route
        object SubscriptionPaywall : Route
        object Onboarding : Route
        data class GoogleReview(val reviewInfo: ReviewInfo) : Route
    }

    fun route(route: Route)

    fun attachHost(activity: FragmentActivity)

    fun detachHost()

}

class RouterImpl @Inject constructor(
    private val preferenceCache: PreferenceCache
) : Router, CoroutineScope by MainScope() {

    private val routeQueue = ConcurrentLinkedQueue<Router.Route>()

    private var host: FragmentActivity? = null

    override fun attachHost(activity: FragmentActivity) {
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
                    is Router.Route.Pack -> showPackDialog(route.pack)
                    is Router.Route.PackHistory -> showPackHistory(route.pack)
                    is Router.Route.ProvideUserInfo -> showUserInfo()
                    is Router.Route.Finish -> finish()
                    is Router.Route.SubscriptionPaywall -> showSubscriptionPaywall()
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

    private fun showPackDialog(pack: Pack) {
        host?.let { activity ->
            PackDialog.show(
                activity.supportFragmentManager,
                pack
            )
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

    private fun showSubscriptionPaywall() {
        host?.let { activity ->
            val intent = Intent(activity, SubscriptionPaywallActivity::class.java)
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
                preferenceCache.putBoolean(PreferenceKey.GoogleStoreReviewShown, true)
            }
        }
    }

    private fun finish() {
        host?.finish()
    }

}