package com.izum.domain.core

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface PreferenceCache {

    fun putString(key: PreferenceKey, value: String?)

    fun putBoolean(key: PreferenceKey, value: Boolean)

    fun getString(key: PreferenceKey) : String?

    fun getBoolean(key: PreferenceKey, fallback: Boolean) : Boolean

}

enum class PreferenceKey {
    Token,
    HasSubscription,
    UserInfoProvided,
    IsAdult
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

    override fun putBoolean(key: PreferenceKey, value: Boolean) {
        preferences.edit()
            .putBoolean(key.name, value)
            .apply()
    }

    override fun getString(key: PreferenceKey): String? {
        return preferences.getString(key.name, "")
    }

    override fun getBoolean(key: PreferenceKey, fallback: Boolean) : Boolean {
        return preferences.getBoolean(key.name, fallback)
    }

}