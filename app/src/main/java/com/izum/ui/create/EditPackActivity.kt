package com.izum.ui.create

import android.app.AlertDialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.izum.databinding.ActivityEditPackBinding
import com.izum.ui.BaseActivity
import com.izum.ui.KEY_ARGS_INPUT
import com.izum.ui.poll.list.PollsAdapter
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
            onPollClick = {  },
            onButtonClick = viewModel::onAddPollClick
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
        ivShare.setOnClickListener { viewModel.onShareClick(
            clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
        )}
        tvRetry.setOnClickListener { viewModel.onRetryClick() }
        tvRemove.setOnClickListener { onRemoveClick() }

        rvPolls.adapter = adapter
        rvPolls.layoutManager = LinearLayoutManager(this@EditPackActivity, LinearLayoutManager.VERTICAL, false)

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

    private fun onRemoveClick() {
        removeDialog?.hide()
        removeDialog = null
        removeDialog = AlertDialog.Builder(this)
            .setTitle("Are you sure?")
            .setMessage("Do you really want to remove this pack?")
            .setPositiveButton("Yes") { dialog, _ ->
                viewModel.onRemoveApprovedClick()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        removeDialog?.show()
    }

}