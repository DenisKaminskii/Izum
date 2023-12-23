package com.pickone

import android.app.Application
import com.pickone.data.repository.UserRepository
import com.pickone.domain.billing.Billing
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PickOneApplication : Application() {

    @Inject lateinit var billing: Billing
    @Inject lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        initBilling()
        auth()
    }

    private fun initBilling() {
        billing.init()
    }

    private fun auth() = GlobalScope.launch {
        userRepository.auth()
    }

}