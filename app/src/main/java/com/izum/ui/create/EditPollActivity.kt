package com.izum.ui.create

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.izum.databinding.ActivityEditPollBinding
import com.izum.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPollActivity : BaseActivity() {

    companion object {
        const val KEY_ARGS_EDIT_POLL_VARIANT = "KEY_ARGS_EDIT_POLL_VARIANT"
    }

    private var _binding: ActivityEditPollBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPollViewModel by viewModels()

    private lateinit var variant: EditPollVariant

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityEditPollBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initArgs(args: Bundle) {
        super.initArgs(args)
        variant = args.getParcelable(KEY_ARGS_EDIT_POLL_VARIANT)!!
    }

    override fun initView() {
        super.initView()

        binding.vBack.setOnClickListener { viewModel.onBackClick() }
        binding.tvAction.setOnClickListener { viewModel.onActionClick() }

        update(viewModel.viewState)

        viewModel.onViewInitialized(variant)
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel, ::update)
    }

    private fun update(state: EditPollViewState) {
        binding.vLoading.isVisible = state is EditPollViewState.Loading
        binding.tvAction.isVisible = state is EditPollViewState.Input

        if (state !is EditPollViewState.Input) return

        binding.tvAction.isEnabled = state.isDoneEnabled
    }

}