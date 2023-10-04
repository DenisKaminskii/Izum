package com.polleo.ui.packs

import androidx.lifecycle.viewModelScope
import com.polleo.data.Pack
import com.polleo.data.repository.CustomPacksRepository
import com.polleo.data.repository.PublicPacksRepository
import com.polleo.data.repository.UserRepository
import com.polleo.di.IoDispatcher
import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.StateViewModel
import com.polleo.ui.ViewAction
import com.polleo.ui.create.EditPackInput
import com.polleo.ui.edit.EditPollVariant
import com.polleo.ui.pack.history.PackHistoryInput
import com.polleo.ui.route.Router
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PacksViewState {

    object Loading : PacksViewState

    data class Packs(
        val publicPacks: List<Pack.Public>,
        val customPacks: List<Pack.Custom>,
        val hasSubscription: Boolean
    ) : PacksViewState
}


@HiltViewModel
class PacksViewModel @Inject constructor(
    private val publicPacksRepository: PublicPacksRepository,
    private val customPacksRepository: CustomPacksRepository,
    private val userRepository: UserRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StateViewModel<Unit, PacksViewState>(
    initialState = PacksViewState.Loading
) {

    private val publicPacks = mutableListOf<Pack.Public>()
    private val customPacks = mutableListOf<Pack.Custom>()

    private val hasSubscription: Boolean
        get() = userRepository.hasSubscription

    override fun onViewInitialized(input: Unit) {
        super.onViewInitialized(input)

        viewModelScope.launch {
            publicPacksRepository.packs
                .collect { publicPacks ->
                    this@PacksViewModel.publicPacks.clear()
                    this@PacksViewModel.publicPacks.addAll(publicPacks)
                    updateView()
                }
        }

        viewModelScope.launch {
            customPacksRepository.packs
                .collect { customPacks ->
                    this@PacksViewModel.customPacks.clear()
                    this@PacksViewModel.customPacks.addAll(customPacks)
                    updateView()
                }
        }
    }

    fun onStart() {
        fetchPacks()
    }

    private fun fetchPacks() = viewModelScope.launch(ioDispatcher) {
        publicPacksRepository.fetch()
        customPacksRepository.fetch()
    }

    private fun updateView() {
        updateState {
            PacksViewState.Packs(
                publicPacks = publicPacks,
                customPacks = customPacks,
                hasSubscription = hasSubscription
            )
        }
    }

    fun onPublicPackClick(pack: Pack) {
        if (pack !is Pack.Public) return

        viewModelScope.launch {
            route(Router.Route.Pack(pack))
        }
    }

    fun onCustomPackClick(pack: Pack) {
        if (pack !is Pack.Custom) return

        viewModelScope.launch {
            route(Router.Route.EditPack(
                input = EditPackInput(
                    packId = pack.id,
                    packTitle = pack.title,
                    shareLink = (pack as? Pack.Custom)?.link ?: ""
                )
            ))
        }
    }

    fun onStartClick(publicPack: Pack.Public) {
        viewModelScope.launch {
            route(Router.Route.Polls(publicPack.id, publicPack.title))
        }
    }

    fun onPackHistoryClick(publicPack: Pack.Public) {
        viewModelScope.launch {
            route(Router.Route.PackHistory(
                input = PackHistoryInput(
                    packId = publicPack.id,
                    packTitle = publicPack.title
                )
            ))
        }
    }

    fun onSubscribeClick() {
        viewModelScope.launch {
            route(Router.Route.SubscriptionPaywall)
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
                    shareLink = newPackLink
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