package com.izum.data.user

import android.os.Parcelable
import com.izum.data.DeviceIdProvider
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.MutableStateProducer
import com.izum.domain.core.PreferenceKey
import com.izum.domain.core.StateProducer
import com.izum.api.AuthApi
import com.izum.api.GetTokenRequest
import kotlinx.parcelize.Parcelize
import java.lang.Exception
import javax.inject.Inject

interface UserRepository : StateProducer<UserRepository.State> {

    sealed interface State {
        @Parcelize
        data class Authorized(
            val token: String
        ) : State, Parcelable

        object UnAuthorized : State
    }

    @Throws(Exception::class)
    suspend fun fetchToken()

}

class UserRepositoryImpl @Inject constructor(
    private val preferenceCache: PreferenceCache,
    private val authApi: AuthApi,
    private val deviceIdProvider: DeviceIdProvider
) : UserRepository,
    MutableStateProducer<UserRepository.State>(
        initialValue = UserRepository.State.UnAuthorized
    ) {

    override suspend fun fetchToken() {
        val response = authApi.getToken(
            GetTokenRequest(
                deviceId = deviceIdProvider.deviceId
            )
        )

        val token = response.token
        preferenceCache.putString(PreferenceKey.Token, token)
        updateState {
            UserRepository.State.Authorized(token)
        }
    }


}

fun UserRepository.State.ifAuthorized(func: (state: UserRepository.State.Authorized) -> Unit): UserRepository.State {
    if (this is UserRepository.State.Authorized) {
        func.invoke(this)
    }
    return this
}

fun UserRepository.State.ifUnAuthorized(func: (state: UserRepository.State.UnAuthorized) -> Unit): UserRepository.State {
    if (this is UserRepository.State.UnAuthorized) {
        func.invoke(this)
    }
    return this
}