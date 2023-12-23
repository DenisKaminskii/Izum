package com.pickone.data.repository

import com.pickone.api.UpdateUserInfoRequest
import com.pickone.api.UserApi
import com.pickone.di.IoDispatcher
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
import com.pickone.ui.user.Gender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UserRepository  {

    var hasSubscription: Boolean

    var isStatisticInfoProvided: Boolean

    val userId: Long?

    suspend fun updateUserInfo(age: Int, gender: Gender) : Boolean

}

class UserRepositoryImpl(
    private val preferenceCache: PreferenceCache,
    private val userApi: UserApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override var hasSubscription: Boolean
        get() = true // preferenceCache.getBoolean(key = PreferenceKey.HasSubscription, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.HasSubscription.name, value)
        }

    override var isStatisticInfoProvided: Boolean
        get() = preferenceCache.getBoolean(key = PreferenceKey.UserInfoProvided.name, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.UserInfoProvided.name, value)
        }

    override val userId: Long?
        get() = preferenceCache.getLongOrNull(PreferenceKey.UserId.name)

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
            false
        }
    }


}