package com.polleo.ui.edit

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.polleo.databinding.ActivityEditPollBinding
import com.polleo.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPollActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_EDIT_POLL_VARIANT = "KEY_ARGS_EDIT_POLL_VARIANT"
    }

    private var _binding: ActivityEditPollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPollViewModel by viewModels()

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityEditPollBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) {
        super.initView(args)

        binding.vBack.setOnClickListener { viewModel.onBackClick() }
        binding.tvAction.setOnClickListener { viewModel.onActionClick() }

        update(viewModel.viewState)

        viewModel.onViewInitialized(
            input = args.getParcelable(KEY_ARGS_EDIT_POLL_VARIANT)!!
        )
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel, ::update)
    }

    private fun update(state: EditPollViewState) {
        binding.vLoading.isVisible = state is EditPollViewState.Loading
        binding.tvAction.isVisible = state is EditPollViewState.Input

        if (state !is EditPollViewState.Input) return

        binding.tvToolbar.text = state.title
        binding.tvAction.isEnabled = state.isDoneEnabled
        binding.tvAction.text = state.actionText
        binding.tvSuggestDescription.text = state.suggestText

        binding.tvAction.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(state.actionDrawableId), null)
    }

}