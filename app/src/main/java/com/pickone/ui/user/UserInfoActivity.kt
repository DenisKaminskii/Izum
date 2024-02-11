package com.pickone.ui.user

import android.os.Bundle
import android.text.Editable
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.pickone.analytics.Analytics
import com.pickone.databinding.ActivityUserInfoBinding
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
import com.pickone.ui.BaseActivity
import com.pickone.ui.Keyboard
import com.pickone.ui.SimpleTextWatcher
import com.pickone.ui.route.Router
import com.pickone.ui.user.UserInfoViewModel.Companion.AGE_LIMIT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// § Rename: InputInfoActivity
@AndroidEntryPoint
class UserInfoActivity : BaseActivity() {

    @Inject lateinit var preferenceCache: PreferenceCache
    @Inject lateinit var analytics: Analytics

    private var _binding: ActivityUserInfoBinding? = null
    val binding: ActivityUserInfoBinding get() = _binding!!

    private val viewModel: UserInfoViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        super.initView(args)
        binding.root.setOnClickListener {
            Keyboard.hideSoftKeyboard(binding.tieAge)
            binding.tieAge.clearFocus()
        }

        binding.rbFemale.setOnCheckedChangeListener { buttonView, isChecked -> viewModel.onGenderSelect(Gender.FEMALE) }
        binding.rbMale.setOnCheckedChangeListener { buttonView, isChecked -> viewModel.onGenderSelect(Gender.MALE) }
        binding.rbOther.setOnCheckedChangeListener { buttonView, isChecked -> viewModel.onGenderSelect(Gender.OTHER) }

        binding.tieAge.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                val currentText = s.toString()

                try {
                    val enteredAge = currentText.toInt()

                    if (enteredAge > AGE_LIMIT) {
                        binding.tieAge.removeTextChangedListener(this)
                        binding.tieAge.setText(currentText.dropLast(1))
                        binding.tieAge.setSelection(binding.tieAge.text?.length ?: 0)
                        binding.tieAge.addTextChangedListener(this)
                    } else {
                        viewModel.onAgeInput(enteredAge)
                    }
                } catch (e: NumberFormatException) {
                    viewModel.onAgeInput(null)

                    if (currentText.isNotEmpty()) {
                        binding.tieAge.removeTextChangedListener(this)
                        binding.tieAge.setText("")
                        binding.tieAge.addTextChangedListener(this)
                    }
                }
            }
        })

        binding.tvFinish.setOnClickListener { viewModel.onFinishClick() }

        update(UserInfoViewState())

        viewModel.onViewInitialized(Unit)

        checkOnboardingShow()
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState)}
    }

    override fun onMovedToBackground() {
        super.onMovedToBackground()
        if (!binding.vProgress.isVisible) {
            analytics.profileSetupExitedApp()
        }
    }

    private fun checkOnboardingShow() {
        val isOnboardingShowed = preferenceCache.getBoolean(PreferenceKey.IsOnboardingShowed.name, false)
        if (!isOnboardingShowed) {
            router.route(Router.Route.Onboarding)
        }
    }

    private fun update(viewState: UserInfoViewState) {
        binding.tvFinish.isEnabled = viewState.isFinishEnabled
        binding.vgRadio.isEnabled = !viewState.isLoadingVisible
        binding.tieAge.isEnabled = !viewState.isLoadingVisible
        binding.tvFinish.isVisible = !viewState.isLoadingVisible
        binding.vProgress.isVisible = viewState.isLoadingVisible
    }

}