package com.izum.ui.paywall

import android.os.Bundle
import com.izum.databinding.ActivitySubscriptionPaywallBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionPaywallActivity : BaseActivity() {

    private var _binding: ActivitySubscriptionPaywallBinding? = null
    private val binding get() = _binding!!

    override fun initLayout() {
        super.initLayout()
        _binding = ActivitySubscriptionPaywallBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) = with(binding) {
        super.initView(args)
    }



}