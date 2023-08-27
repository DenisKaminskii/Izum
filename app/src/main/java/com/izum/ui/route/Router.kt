package com.izum.ui.route

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.izum.data.Pack
import com.izum.data.Poll
import com.izum.ui.KEY_ARGS_INPUT
import com.izum.ui.create.EditPackActivity
import com.izum.ui.create.EditPackInput
import com.izum.ui.edit.EditPollActivity
import com.izum.ui.edit.EditPollVariant
import com.izum.ui.pack.PackDialog
import com.izum.ui.pack.history.PackHistoryActivity
import com.izum.ui.packs.PacksActivity
import com.izum.ui.poll.PollActivity
import com.izum.ui.poll.statistic.PollStatisticActivity
import com.izum.ui.user.UserInfoActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

interface Router {

    sealed interface Route {
        object Packs : Route
        data class Polls(val pack: com.izum.data.Pack) : Route
        data class EditPoll(val variant: EditPollVariant) : Route
        data class EditPack(val input: EditPackInput) : Route
        data class Statistic(val poll: Poll) : Route
        data class Pack(val pack: com.izum.data.Pack.Public) : Route
        data class PackHistory(val publicPack: com.izum.data.Pack.Public) : Route
        object ProvideUserInfo : Route
        object Finish : Route
    }

    fun route(route: Route)

    fun attachHost(activity: FragmentActivity)

    fun detachHost()

}

class RouterImpl @Inject constructor() : Router, CoroutineScope by MainScope() {

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
                    is Router.Route.Statistic -> showPollStatistic(route.poll)
                    is Router.Route.Pack -> showPackDialog(route.pack)
                    is Router.Route.PackHistory -> showPackHistory(route.publicPack)
                    is Router.Route.ProvideUserInfo -> showUserInfo()
                    is Router.Route.Finish -> finish()
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
            intent.putExtra(PollActivity.KEY_ARGS_PACK_ID, pack.id)
            intent.putExtra(PollActivity.KEY_ARGS_PACK_TITLE, pack.title)
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

    private fun showPollStatistic(poll: Poll) {
        host?.let { activity ->
            val intent = Intent(activity, PollStatisticActivity::class.java)
            intent.putExtra(PollStatisticActivity.KEY_ARGS_POLL, poll)
            activity.startActivity(intent)
        }
    }

    private fun showPackDialog(publicPack: Pack.Public) {
        host?.let { activity ->
            PackDialog.show(
                activity.supportFragmentManager,
                publicPack
            )
        }
    }

    private fun showPackHistory(publicPack: Pack.Public) {
        host?.let { activity ->
            val intent = Intent(activity, PackHistoryActivity::class.java)
            intent.putExtra(PackHistoryActivity.KEY_ARGS_PACK, publicPack)
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

    private fun finish() {
        host?.finish()
    }

}