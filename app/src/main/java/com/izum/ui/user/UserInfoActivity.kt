package com.izum.ui.user

import android.text.Editable
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.izum.databinding.ActivityUserInfoBinding
import com.izum.ui.BaseActivity
import com.izum.ui.Keyboard
import com.izum.ui.SimpleTextWatcher
import com.izum.ui.user.UserInfoViewModel.Companion.AGE_LIMIT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoActivity : BaseActivity() {

    private var _binding: ActivityUserInfoBinding? = null
    val binding: ActivityUserInfoBinding get() = _binding!!

    private val viewModel: UserInfoViewModel by viewModels()

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
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { viewState -> update(viewState)}
    }

    private fun update(viewState: UserInfoViewState) {
        binding.tvFinish.isEnabled = viewState.isFinishEnabled
        binding.vgRadio.isEnabled = !viewState.isLoadingVisible
        binding.tieAge.isEnabled = !viewState.isLoadingVisible
        binding.tvFinish.isVisible = !viewState.isLoadingVisible
        binding.vProgress.isVisible = viewState.isLoadingVisible
    }

}