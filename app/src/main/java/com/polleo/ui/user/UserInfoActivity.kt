package com.polleo.ui.user

import android.os.Bundle
import android.text.Editable
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.polleo.databinding.ActivityUserInfoBinding
import com.polleo.domain.core.PreferenceCache
import com.polleo.domain.core.PreferenceKey
import com.polleo.ui.BaseActivity
import com.polleo.ui.Keyboard
import com.polleo.ui.SimpleTextWatcher
import com.polleo.ui.route.Router
import com.polleo.ui.user.UserInfoViewModel.Companion.AGE_LIMIT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoActivity : BaseActivity() {

    private var _binding: ActivityUserInfoBinding? = null
    val binding: ActivityUserInfoBinding get() = _binding!!

    private val viewModel: UserInfoViewModel by viewModels()

    @Inject lateinit var preferenceCache: PreferenceCache

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

    private fun checkOnboardingShow() {
        val isOnboardingShowed = preferenceCache.getBoolean(PreferenceKey.IsOnboardingShowed, false)
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