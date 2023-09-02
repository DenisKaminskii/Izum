package com.izum.ui.packs

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayoutMediator
import com.izum.data.repository.UserRepository
import com.izum.databinding.ActivityPacksBinding
import com.izum.ui.BaseActivity
import com.izum.ui.create.PackTitleEditDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PacksActivity : BaseActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    private var _binding: ActivityPacksBinding? = null
    private val binding: ActivityPacksBinding
        get() = _binding!!

    private val viewModel: PacksViewModel by viewModels()

    private val viewPagerAdapter: PacksViewPagerAdapter by lazy {
        PacksViewPagerAdapter(supportFragmentManager, lifecycle, ::onCreatePackClick)
    }

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityPacksBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        binding.vgPager.adapter = viewPagerAdapter

        binding.vgSubscription.setOnClickListener { viewModel.onSubscribeClick() }
        binding.vCreatePack.setOnClickListener { onCreatePackClick() }
        binding.vSuggest.setOnClickListener { viewModel.onSuggestPollClick() }

        TabLayoutMediator(binding.vgTabs, binding.vgPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Public"
                1 -> "My Packs"
                else -> ""
            }
        }.attach()

        update(PacksViewState.Loading)

        viewModel.onViewInitialized(Unit)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("Steve", "Junk!")
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchPacks()
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState) }
    }

    private fun update(viewState: PacksViewState) {
        if (viewState !is PacksViewState.Packs) return

        val hasSubscription = viewState.hasSubscription
        binding.tvSubscribe.isVisible = !hasSubscription
        binding.tvSubscribed.isVisible = hasSubscription
    }

    private var createPackDialog: PackTitleEditDialog? = null

    private fun onCreatePackClick() {
        createPackDialog?.dismissAllowingStateLoss()
        createPackDialog = null
        createPackDialog = PackTitleEditDialog.getInstance(
            title = "",
            onApply = { title ->
                viewModel.onCreatePack(title)
            },
            onCancel = {
                createPackDialog = null
            }
        )
        createPackDialog?.show(supportFragmentManager, "PackTitleEditDialog")
    }

}