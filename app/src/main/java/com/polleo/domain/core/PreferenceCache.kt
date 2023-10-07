package com.polleo.domain.core

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface PreferenceCache {

    fun putString(key: PreferenceKey, value: String?)

    fun putString(key: String, value: String?)

    fun putBoolean(key: PreferenceKey, value: Boolean)

    fun putLong(key: String, value: Long)

    fun getString(key: PreferenceKey) : String?

    fun getString(key: String) : String?

    fun getBoolean(key: PreferenceKey, fallback: Boolean) : Boolean

    fun getLong(key: String, fallback: Long) : Long

    fun getLongOrNull(key: String) : Long?

}

enum class PreferenceKey {
    Token,
    HasSubscription,
    UserInfoProvided,
    IsAdult,
    IsOnboardingShowed,
    CommonVotesCount,
    GoogleStoreReviewShown
}

class PreferenceCacheImpl @Inject constructor(
    @ApplicationContext context: Context
) : PreferenceCache {

    companion object {
        private const val KEY_IZUM_CACHE = "Izum"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(KEY_IZUM_CACHE, Context.MODE_PRIVATE)

    override fun putString(key: PreferenceKey, value: String?) {
        preferences.edit()
            .putString(key.name, value)
            .apply()
    }

    override fun putString(key: String, value: String?) {
        preferences.edit()
            .putString(key, value)
            .apply()
    }

    override fun putBoolean(key: PreferenceKey, value: Boolean) {
        preferences.edit()
            .putBoolean(key.name, value)
            .apply()
    }

    override fun putLong(key: String, value: Long) {
        preferences.edit()
            .putLong(key, value)
            .apply()
    }

    override fun getString(key: PreferenceKey): String? {
        return preferences.getString(key.name, "")
    }

    override fun getString(key: String): String? {
        return preferences.getString(key, "")
    }

    override fun getBoolean(key: PreferenceKey, fallback: Boolean) : Boolean {
        return preferences.getBoolean(key.name, fallback)
    }

    override fun getLong(key: String, fallback: Long) : Long {
        return preferences.getLong(key, fallback)
    }

    override fun getLongOrNull(key: String) : Long? {
        val res = preferences.getLong(key, 0)
        return if (res == 0L) null else res
    }

}