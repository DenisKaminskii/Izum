package com.polleo.ui.create

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.polleo.R
import com.polleo.databinding.ActivityEditPackBinding
import com.polleo.ui.BaseActivity
import com.polleo.ui.KEY_ARGS_INPUT
import com.polleo.ui.poll.list.PollsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditPackInput(
    val packId: Long,
    val packTitle: String? = null,
    val shareLink: String
) : Parcelable

@AndroidEntryPoint
class EditPackActivity : BaseActivity() {

    private var _binding: ActivityEditPackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPackViewModel by viewModels()

    private val adapter by lazy {
        PollsAdapter(
            onCustomPackPollClick = viewModel::onPollClick,
            onSubscribeClick = viewModel::onSubscribeClick,
            onCustomPackPollSelected = viewModel::onPollItemSelected
        )
    }

    private var titleEditDialog: PackTitleEditDialog? = null
    private var removeDialog: AlertDialog? = null

    override fun initLayout() {
        super.initLayout()
        _binding = ActivityEditPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun initView(args: Bundle) = with(binding) {
        super.initView(args)
        tvPackTitle.setOnClickListener { onTitleClick() }
        ivBack.setOnClickListener { viewModel.onBackClick() }
        ivShare.setOnClickListener {
            viewModel.onShareClick(
                clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
            )
        }
        tvRetry.setOnClickListener { viewModel.onRetryClick() }
        tvRemove.setOnClickListener { onPackRemoveClick() }
        tvAdd.setOnClickListener { viewModel.onAddPollClick() }

        rvPolls.adapter = adapter
        rvPolls.layoutManager =
            LinearLayoutManager(this@EditPackActivity, LinearLayoutManager.VERTICAL, false)

        onBackPressedDispatcher.addCallback(this@EditPackActivity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
            }
        })

        update(EditPackViewState.Loading)

        viewModel.onViewInitialized(
            input = args.getParcelable(KEY_ARGS_INPUT)!!
        )
    }

    override fun initSubs() {
        super.initSubs()
        subscribe(viewModel) { state -> update(state) }
    }

    private fun update(state: EditPackViewState) = with(binding) {
        gContent.isVisible = state is EditPackViewState.Content
        vProgress.isVisible = state is EditPackViewState.Loading
        vgError.isVisible = state is EditPackViewState.Error

        if (state !is EditPackViewState.Content) return

        tvPackTitle.text = state.title
        tvPollsCount.text = "${state.pollsCount} / ${state.pollsMax}"
        ivShare.isEnabled = state.isShareButtonEnabled
        ivEdit.isEnabled = state.isEditButtonEnabled
        ivEdit.setImageResource(
            when {
                state.isEditMode -> if (state.isEditRemoveVisible) {
                    R.drawable.ic_remove
                } else {
                    R.drawable.ic_close_24
                }
                else -> R.drawable.ic_edit
            }
        )
        ivEdit.imageTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(
                if (state.isEditRemoveVisible) getColor(R.color.red)
                else getColor(R.color.white)
            )
        )
        ivEdit.setOnClickListener {
            if (state.isEditMode) {
                if (state.isEditRemoveVisible) {
                    onPollsRemoveApproved()
                } else {
                    viewModel.onEditCancelClick()
                }
            }
            else viewModel.onEditClick()
        }
        tvAdd.isVisible = state.isAddButtonVisible
        tvAdd.isEnabled = !state.isEditMode
        ivShare.alpha = if (state.isShareButtonEnabled) 1f else 0.25f

        adapter.setItems(state.polls)
    }

    private fun onTitleClick() {
        titleEditDialog?.dismissAllowingStateLoss()
        titleEditDialog = null
        titleEditDialog = PackTitleEditDialog.getInstance(
            title = binding.tvPackTitle.text.toString(),
            onApply = {
                viewModel.onTitleChanged(it)
                titleEditDialog?.dismissAllowingStateLoss()
            },
            onCancel = { }
        )
        titleEditDialog?.show(supportFragmentManager, PackTitleEditDialog::class.java.simpleName)
    }

    private fun onPackRemoveClick() {
        removeDialog?.hide()
        removeDialog = null
        removeDialog = AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to remove this pack?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.onPackRemoveApproved()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        removeDialog?.show()
    }

    private fun onPollsRemoveApproved() {
        removeDialog?.hide()
        removeDialog = null
        removeDialog = AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to remove this questions?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.onRemovePollsApproved()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        removeDialog?.show()
    }

}