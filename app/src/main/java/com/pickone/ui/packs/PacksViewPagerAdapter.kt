package com.pickone.ui.packs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pickone.ui.profile.ProfileFragment

class PacksViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val onCreatePackClick: () -> Unit
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> PacksFragment.newInstance(PacksInput(isCustom = false))
            1 -> PacksFragment.newInstance(PacksInput(isCustom = true), onCreatePackClick = onCreatePackClick)
            2 -> ProfileFragment.newInstance()
            else -> throw IllegalArgumentException("Unknown position: $position")
        }
    }

}