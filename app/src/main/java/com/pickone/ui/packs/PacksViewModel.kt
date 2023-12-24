package com.pickone.ui.packs

import androidx.lifecycle.viewModelScope
import com.pickone.data.Pack
import com.pickone.data.repository.CustomPacksRepository
import com.pickone.data.repository.PublicPacksRepository
import com.pickone.data.repository.UserRepository
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.StateViewModel
import com.pickone.ui.ViewAction
import com.pickone.ui.create.EditPackInput
import com.pickone.ui.edit.EditPollVariant
import com.pickone.ui.pack.PackDialogInput
import com.pickone.ui.paywall.OnPremiumPurchased
import com.pickone.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PacksViewState(
    val publicPacks: List<Pack.Public>? = null,
    val customPacks: List<Pack.Custom>? = null,
    val hasSubscription: Boolean = true
)


@HiltViewModel
class PacksViewModel @Inject constructor(
    private val publicPacksRepository: PublicPacksRepository,
    private val customPacksRepository: CustomPacksRepository,
    private val userRepository: UserRepository,
    private val preferenceCache: PreferenceCache,
    private val onPremiumPurchased: OnPremiumPurchased,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StateViewModel<Unit, PacksViewState>(
    initialState = PacksViewState()
) {

    private val publicPacks = mutableListOf<Pack.Public>()
    private val customPacks = mutableListOf<Pack.Custom>()

    private var isPublicFetched = false
    private var isCustomFetched = false

    private val hasSubscription: Boolean
        get() = userRepository.hasSubscription

    override fun onViewInitialized(input: Unit) {
        super.onViewInitialized(input)

        viewModelScope.launch {
            publicPacksRepository.packs
                .collect { publicPacks ->
                    this@PacksViewModel.publicPacks.clear()
                    this@PacksViewModel.publicPacks.addAll(publicPacks)
                    isPublicFetched = true
                    updateView()
                }
        }

        viewModelScope.launch {
            customPacksRepository.packs
                .collect { customPacks ->
                    this@PacksViewModel.customPacks.clear()
                    this@PacksViewModel.customPacks.addAll(customPacks.myPacks)
                    this@PacksViewModel.customPacks.addAll(customPacks.addedPacks)
                    isCustomFetched = true
                    updateView()
                }
        }

        viewModelScope.launch {
            onPremiumPurchased
                .collect { updateView() }
        }
    }

    fun onStart() = viewModelScope.launch(ioDispatcher) {
        launch { publicPacksRepository.fetchFeed() }
        launch { customPacksRepository.fetchMyPacks() }
        launch { customPacksRepository.fetchAddedPacks() }
    }

    private fun updateView() {
        updateState {
            PacksViewState(
                publicPacks = if (isPublicFetched) publicPacks else null,
                customPacks = if (isCustomFetched) customPacks else null,
                hasSubscription = hasSubscription
            )
        }
    }

    fun onPublicPackClick(pack: Pack) {
        if (pack !is Pack.Public) return

        viewModelScope.launch {
            route(
                Router.Route.Pack(
                    PackDialogInput(
                        pack = pack,
                        answeredPollsCount = preferenceCache.getLongOrNull("${pack.id}_voted_count") ?: 0
                    )
                )
            )
        }
    }

    fun onCustomPackClick(pack: Pack) {
        if (pack !is Pack.Custom) return

        viewModelScope.launch {
            if (pack.isMine) {
                route(
                    Router.Route.EditPack(
                        input = EditPackInput(
                            packId = pack.id,
                            packTitle = pack.title,
                            packCode = (pack as? Pack.Custom)?.code ?: ""
                        )
                    )
                )
            } else {
                route(
                    Router.Route.Pack(
                        PackDialogInput(
                            pack = pack,
                            answeredPollsCount = preferenceCache.getLongOrNull("${pack.id}_voted_count") ?: 0
                        )
                    )
                )
            }
        }
    }

    fun onNewCustomPackStartClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.Polls(pack))
        }
    }

    fun onNewCustomPackStartClick(packId: Long) {
        val pack = customPacks.firstOrNull { it.id == packId } ?: return

        viewModelScope.launch {
            route(Router.Route.Polls(pack))
        }
    }

    fun onPackHistoryClick(pack: Pack) {
        viewModelScope.launch {
            route(Router.Route.PackHistory(pack))
        }
    }

    fun onSubscribeClick() {
        viewModelScope.launch {
            route(Router.Route.Paywall)
        }
    }

    fun onCreatePack(title: String) {
        viewModelScope.launch {
            try {
                val newPackData = customPacksRepository.createPack(title)
                val newPackId = newPackData.first
                val newPackLink = newPackData.second

                route(Router.Route.EditPack(EditPackInput(
                    packId = newPackId,
                    packTitle = title,
                    packCode = newPackLink
                )))
            } catch (ex: Exception) {
                emit(ViewAction.ShowToast("Error creating pack. Try again."))
            }
        }
    }

    fun onSuggestPollClick() {
        viewModelScope.launch {
            route(Router.Route.EditPoll(EditPollVariant.Suggest))
        }
    }

}