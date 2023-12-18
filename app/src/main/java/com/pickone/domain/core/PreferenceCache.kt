package com.pickone.domain.core

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
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

    fun <T> putDTO(name: String, value: T, clazz: Class<T>)

    fun <T> getDTO(name: String, clazz: Class<T>): T?

    fun <T> putList(name: String, value: List<T>, clazz: Class<T>)

    fun <T> getList(name: String, clazz: Class<T>): List<T>?

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
    @ApplicationContext context: Context,
    private val moshi: Moshi
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

    override fun <T> putDTO(name: String, value: T, clazz: Class<T>) {
        val serializedValue = moshi.adapter(clazz).toJson(value)

        preferences.edit()
            .putString(name, serializedValue)
            .apply()
    }

    override fun <T> getDTO(name: String, clazz: Class<T>): T? {
        var result: T? = null
        if (preferences.contains(name)) {
            try {
                result = moshi.adapter(clazz).fromJson(preferences.getString(name, null)!!)
            } catch (e: Exception) {
                preferences.edit()
                    .remove(name)
                    .apply()
            }

        }

        return result
    }

    override fun <T> putList(name: String, value: List<T>, clazz: Class<T>) {
        val type = Types.newParameterizedType(List::class.java, clazz)
        val json = moshi.adapter<Any>(type).toJson(value)
        preferences.edit()
            .putString(name, json)
            .apply()
    }

    override fun <T> getList(name: String, clazz: Class<T>): List<T>? {
        var result: List<T>? = null
        if (preferences.contains(name)) {
            try {
                val type = Types.newParameterizedType(List::class.java, clazz)
                val json = preferences.getString(name, "") ?: ""
                result = moshi.adapter<List<T>>(type).fromJson(json)
            } catch (e: Exception) {
                e.printStackTrace()
                preferences.edit()
                    .remove(name)
                    .apply()
            }

        }
        return result
    }

}