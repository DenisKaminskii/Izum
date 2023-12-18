package com.pickone.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface DeviceIdProvider {

    val deviceId: String

}

class DeviceIdProviderImpl @Inject constructor (
    @ApplicationContext private val context: Context
) : DeviceIdProvider {

    override val deviceId: String
        get() = "SOME_TEST_DEVICE_ID_12" // Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

}