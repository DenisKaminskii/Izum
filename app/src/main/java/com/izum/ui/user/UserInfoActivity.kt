package com.izum.ui.user

import com.izum.databinding.ActivityUserInfoBinding
import com.izum.ui.BaseActivity
import com.izum.ui.Keyboard
import dagger.hilt.android.AndroidEntryPoint

// 1. Token
// 2. Input Validation
// 3. Patch to server
// 4. Open Packs
@AndroidEntryPoint
class UserInfoActivity : BaseActivity() {

    private var _binding: ActivityUserInfoBinding? = null
    val binding: ActivityUserInfoBinding get() = _binding!!

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView() {
        super.initView()
        binding.root.setOnClickListener {
            Keyboard.hideSoftKeyboard(binding.tieAge)
            binding.tieAge.clearFocus()
        }
    }

}