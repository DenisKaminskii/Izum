package com.izum.ui.pack

import android.app.Dialog
import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.ColorInt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.izum.R
import com.izum.data.Pack
import com.izum.databinding.DialogFragmentPackBinding
import com.izum.ui.BaseDialogFragment
import com.izum.ui.dpF
import com.izum.ui.getBackgroundGradient
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PackFragment : BaseDialogFragment() {

    companion object {

        private const val KEY_ARGS_PACK = "KEY_ARGS_PACK"

        fun show(
            fragmentManager: FragmentManager,
            pack: Pack
        ) {
            PackFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_ARGS_PACK, pack)
                }
            }.show(fragmentManager, "PackFragment")
        }
    }

    private var _binding: DialogFragmentPackBinding? = null
    private val binding: DialogFragmentPackBinding
        get() = _binding!!

    private lateinit var pack: Pack

    private val previewAdapter by lazy { PackPreviewAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog)

        try {
            pack = requireArguments().getParcelable(KEY_ARGS_PACK)!!
        } catch (e: Exception) {
            Log.d("Steve", "PackFragment onCreate: pack is null. Exception: $e")
            dismiss()
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentPackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(
            createInsetDrawable(
                color = requireContext().getColor(R.color.purple),
                cornerRadius = requireContext().dpF(24),
                leftInset = requireContext().dpF(16),
                rightInset = requireContext().dpF(16)
            )
        )
        return dialog
    }

    private fun createInsetDrawable(color: Int, cornerRadius: Float, leftInset: Float, rightInset: Float): Drawable {
        val baseDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            this.cornerRadius = cornerRadius
        }

        return InsetDrawable(baseDrawable, leftInset.toInt(), 0, rightInset.toInt(), 0)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.rvPreview.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvPreview.adapter = previewAdapter
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvPreview)

        previewAdapter.setItems(
            listOf(
                PackPreviewItem(
                    topText = "Что-то типо того",
                    bottomText = "Или что-то типо этого"
                ),
                PackPreviewItem(
                    topText = "Что-то типо того [1]",
                    bottomText = "Или что-то типо этого [1]"
                ),
                PackPreviewItem(
                    topText = "Что-то типо того [2]",
                    bottomText = "Или что-то типо этого [2]"
                )
            )
        )
    }

}