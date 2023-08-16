package com.izum.data.repository

import com.izum.api.UpdateUserInfoRequest
import com.izum.api.UserApi
import com.izum.di.IoDispatcher
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceKey
import com.izum.ui.user.Gender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface UserRepository  {

    var hasSubscription: Boolean

    var isStatisticInfoProvided: Boolean

    suspend fun updateUserInfo(age: Int, gender: Gender) : Boolean

}

class UserRepositoryImpl(
    private val preferenceCache: PreferenceCache,
    private val userApi: UserApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override var hasSubscription: Boolean
        get() = preferenceCache.getBoolean(key = PreferenceKey.HasSubscription, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.HasSubscription, value)
        }

    override var isStatisticInfoProvided: Boolean
        get() = preferenceCache.getBoolean(key = PreferenceKey.UserInfoProvided, fallback = false)
        set(value) {
            preferenceCache.putBoolean(PreferenceKey.UserInfoProvided, value)
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
            false
        }
    }


}