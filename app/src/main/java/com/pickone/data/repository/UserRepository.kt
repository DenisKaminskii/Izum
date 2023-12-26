package com.pickone.data.repository

import com.pickone.api.GetTokenRequest
import com.pickone.api.TokenApi
import com.pickone.api.UpdateUserInfoRequest
import com.pickone.api.UserApi
import com.pickone.data.DeviceIdProvider
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
import com.pickone.network.OnUserIdUpdated
import com.pickone.ui.user.Gender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber

interface UserRepository  {

    var hasSubscription: Boolean

    var isStatisticInfoProvided: Boolean

    val userId: Long?

    suspend fun auth()

    suspend fun updateUserInfo(age: Int, gender: Gender) : Boolean

}

class UserRepositoryImpl(
    private val preferenceCache: PreferenceCache,
    private val userApi: UserApi,
    private val tokenApi: TokenApi,
    private val deviceIdProvider: DeviceIdProvider,
    private val onUserIdUpdated: OnUserIdUpdated,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override var hasSubscription: Boolean
        get() = preferenceCache.getBoolean(key = PreferenceKey.HasSubscription.name, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.HasSubscription.name, value)
        }

    override var isStatisticInfoProvided: Boolean
        get() = preferenceCache.getBoolean(key = PreferenceKey.UserInfoProvided.name, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.UserInfoProvided.name, value)
        }

    override var userId: Long?
        get() = preferenceCache.getLongOrNull(PreferenceKey.UserId.name)
        set(value) {
            preferenceCache.putLong(PreferenceKey.UserId.name, value)
        }

    override suspend fun auth() {
        if (userId == null) try {
            val request = GetTokenRequest(deviceIdProvider.deviceId)
            val response = tokenApi.getToken(request)

            preferenceCache.putString(PreferenceKey.Token.name, response.token)
            preferenceCache.putLong(PreferenceKey.UserId.name, response.userId)

            this.userId = response.userId
            onUserIdUpdated.emit(response.userId)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to auth through repository")
        }
    }

    override suspend fun updateUserInfo(age: Int, gender: Gender) : Boolean = withContext(ioDispatcher) {
        try {
            userApi.updateUserInfo(UpdateUserInfoRequest(
                age = age,
                gender = when(gender) {
                    Gender.FEMALE -> "FEMALE"
                    Gender.MALE -> "MALE"
                    Gender.OTHER -> "OTHER"
                }
            ))
            true
        } catch (ex: Exception) {
            Timber.e(ex, "Failed to update user info")
            false
        }
    }


}