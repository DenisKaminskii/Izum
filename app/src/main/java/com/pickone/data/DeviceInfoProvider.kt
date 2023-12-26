package com.pickone.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

interface DeviceInfoProvider {
    val appVersionCode: Int
    val appVersion: String
    val appId: String
    val deviceName: String
    val osVersion: String
    val language: String
}

class DeviceInfoProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider
) : DeviceInfoProvider {

    override val appVersionCode: Int
        get() = try {
            PackageInfoCompat.getLongVersionCode(context.getPackageInfo()).toInt()
        } catch (ex: Throwable) {
            Timber.e(ex, "Failed to get app long version code")
            -1
        }

    override val appVersion: String
        get() = try {
            context.getPackageInfo().versionName
        } catch (ex: Throwable) {
            Timber.e(ex, "Failed to get app version name")
            ""
        }

    override val appId: String
        get() = context.applicationContext.packageName

    override val deviceName: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL

            return if (model.startsWith(manufacturer, ignoreCase = true)) {
                model
            } else {
                "$manufacturer $model"
            }
        }

    override val osVersion: String
        get() = Build.VERSION.RELEASE

    override val language: String
        get() {
            val locale = context.resources.configuration.locales[0]
            return "${locale.language}_${locale.country}"
        }

    private fun Context.getPackageInfo(): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }

}