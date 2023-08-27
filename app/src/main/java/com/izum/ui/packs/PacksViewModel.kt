package com.izum.ui.packs

import androidx.lifecycle.viewModelScope
import com.izum.data.Pack
import com.izum.data.repository.CustomPacksRepository
import com.izum.data.repository.PublicPacksRepository
import com.izum.data.repository.UserRepository
import com.izum.di.IoDispatcher
import com.izum.domain.core.StateViewModel
import com.izum.ui.ViewAction
import com.izum.ui.create.EditPackInput
import com.izum.ui.edit.EditPollVariant
import com.izum.ui.route.Router
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
        fetchPacks()
    }

    private fun fetchPacks() = viewModelScope.launch(ioDispatcher) {
        val packs = publicPacksRepository.getPacks()
        this@PacksViewModel.publicPacks.clear()
        this@PacksViewModel.publicPacks.addAll(packs)

        val customPacks = customPacksRepository.getCustomPacks()
        this@PacksViewModel.customPacks.clear()
        this@PacksViewModel.customPacks.addAll(customPacks)

        updateView()
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
            route(Router.Route.Polls(pack))
        }
    }

    fun onStartClick(publicPack: Pack.Public) {
        viewModelScope.launch {
            route(Router.Route.Polls(publicPack))
        }
    }

    fun onPackHistoryClick(publicPack: Pack.Public) {
        viewModelScope.launch {
            route(Router.Route.PackHistory(publicPack))
        }
    }

    fun onSubscribeClick() {
        viewModelScope.launch {
            userRepository.hasSubscription = !userRepository.hasSubscription
            updateView()
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
                    isNew = true,
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