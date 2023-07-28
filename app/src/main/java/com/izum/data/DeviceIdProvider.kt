package com.izum.data

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface DeviceIdProvider {

    val deviceId: String

}

class DeviceIdProviderImpl @Inject constructor (
    @ApplicationContext private val context: Context
) : DeviceIdProvider {

    override val deviceId: String
        get() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

}