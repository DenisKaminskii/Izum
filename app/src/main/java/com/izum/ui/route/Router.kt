package com.izum.ui.route

import android.content.Intent
import androidx.activity.ComponentActivity
import com.izum.data.Pack
import com.izum.data.Poll
import com.izum.ui.create.SuggestPollActivity
import com.izum.ui.create.pack.CreatePackActivity
import com.izum.ui.packs.PacksActivity
import com.izum.ui.poll.PollActivity
import com.izum.ui.poll.statistic.PollStatisticActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

interface Router {

    sealed interface Route {
        object Packs : Route
        data class Poll(val pack: Pack) : Route
        object CreatePoll : Route
        object CreatePack : Route
        data class Statistic(val poll: com.izum.data.Poll) : Route
    }

    fun route(route: Route)

    fun attachHost(activity: ComponentActivity)

    fun detachHost()

}

class RouterImpl @Inject constructor() : Router, CoroutineScope by MainScope() {

    private val routeQueue = ConcurrentLinkedQueue<Router.Route>()

    private var host: ComponentActivity? = null

    override fun attachHost(activity: ComponentActivity) {
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
                    Router.Route.Packs -> showPacksActivity()
                    is Router.Route.Poll -> showPollActivity(route.pack)
                    Router.Route.CreatePoll -> showCreatePoll()
                    Router.Route.CreatePack -> showCreatePack()
                    is Router.Route.Statistic -> showPollStatistic(route.poll)
                }
            }
        }
    }

    private fun showPacksActivity() {
        host?.let { activity ->
            val intent = Intent(activity, PacksActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    private fun showPollActivity(pack: Pack) {
        host?.let { activity ->
            val intent = Intent(activity, PollActivity::class.java)
            intent.putExtra(PollActivity.KEY_ARGS_PACK_ID, pack.id)
            intent.putExtra(PollActivity.KEY_ARGS_PACK_TITLE, pack.title)
            activity.startActivity(intent)
        }
    }

    private fun showCreatePoll() {
        host?.let { activity ->
            val intent = Intent(activity, SuggestPollActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private fun showCreatePack() {
        host?.let { activity ->
            val intent = Intent(activity, CreatePackActivity::class.java)
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

}