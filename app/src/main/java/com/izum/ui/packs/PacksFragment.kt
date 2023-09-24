package com.izum.ui.packs

import android.app.AlertDialog
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.izum.R
import com.izum.databinding.FragmentPacksBinding
import com.izum.domain.core.PreferenceCache
import com.izum.domain.core.PreferenceKey
import com.izum.ui.KEY_ARGS_INPUT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class PacksInput(
    val isCustom: Boolean
) : Parcelable

@AndroidEntryPoint
class PacksFragment : Fragment(), CoroutineScope by MainScope() {

    companion object {

        private const val ADULT_PACK_ID = 10L

        fun newInstance(
            input: PacksInput,
            onCreatePackClick: (() -> Unit)? = null
        ): PacksFragment {
            val fragment = PacksFragment()
            fragment.onCreatePackClick = onCreatePackClick
            fragment.arguments = Bundle().apply {
                putParcelable(KEY_ARGS_INPUT, input)
            }
            return fragment
        }
    }

    @Inject lateinit var preferenceCache: PreferenceCache

    private var _binding: FragmentPacksBinding? = null
    private val binding get() = _binding!!

    private var onCreatePackClick: (() -> Unit)? = null
    private var isCustom: Boolean = false

    private val viewModel: PacksViewModel by lazy {
        ViewModelProvider(requireActivity())[PacksViewModel::class.java]
    }

    private val adapter by lazy {
        PacksAdapter { pack ->
            if (pack.id == ADULT_PACK_ID) {
                checkAge {
                    if (isCustom) {
                        viewModel.onCustomPackClick(pack)
                    } else {
                        viewModel.onPublicPackClick(pack)
                    }
                }
                return@PacksAdapter
            } else {
                if (isCustom) {
                    viewModel.onCustomPackClick(pack)
                } else {
                    viewModel.onPublicPackClick(pack)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val input: PacksInput = arguments?.getParcelable(KEY_ARGS_INPUT)!!
        isCustom = input.isCustom
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPacksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewStateFlow.collect { state -> update(state) }
            }
        }
    }

    private fun initView() {
        binding.rvPacks.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPacks.adapter = adapter
        binding.tvCreatePack.setOnClickListener { onCreatePackClick?.invoke() }
    }

    private fun update(state: PacksViewState) {
        binding.vProgress.isVisible = state is PacksViewState.Loading

        if (state !is PacksViewState.Packs) return

        adapter.setItems(
            items = if (!isCustom) state.publicPacks.map {
                PacksItem(
                    pack = it,
                    description = it.description ?: "",
                    hasSubscription = state.hasSubscription,
                    title = it.title,
                    gradientStartColor = it.gradientStartColor,
                    gradientEndColor = it.gradientEndColor,
                    contentColor = it.contentColor,
                    pollsCount = it.pollsCount,
                    answeredPollsCount = preferenceCache.getLongOrNull("Pack#${it.id}"),
                    isPaid = it.isPaid
                )
            } else state.customPacks.map {
                PacksItem(
                    pack = it,
                    description = it.description ?: "",
                    hasSubscription = state.hasSubscription,
                    title = it.title,
                    gradientStartColor = requireContext().getColor(R.color.black_gradient_start),
                    gradientEndColor = requireContext().getColor(R.color.black_gradient_end),
                    contentColor = requireContext().getColor(R.color.white),
                    pollsCount = it.pollsCount,
                    answeredPollsCount = preferenceCache.getLongOrNull("Pack#${it.id}"),
                    isPaid = false
                )
            }
        )

        if (isCustom) {
            binding.vgNoPacks.isVisible = state.customPacks.isEmpty()
            binding.rvPacks.isVisible = state.customPacks.isNotEmpty()
        } else {
            binding.vgNoPacks.isVisible = state.publicPacks.isEmpty()
            binding.rvPacks.isVisible = state.publicPacks.isNotEmpty()
        }

    }

    private var ageCheckDialog: AlertDialog? = null

    private fun checkAge(
        onSuccess: () -> Unit
    ) {
        val isAdult = preferenceCache.getBoolean(PreferenceKey.IsAdult, false)
        if (isAdult) {
            onSuccess()
            return
        }

        ageCheckDialog?.hide()
        ageCheckDialog = null
        ageCheckDialog = AlertDialog.Builder(requireContext())
            .setTitle("Are you 18 years old or above?")
            .setMessage("Content contains obscene language and is not suitable for kids.")
            .setPositiveButton("Yes") { dialog, _ ->
                preferenceCache.putBoolean(PreferenceKey.IsAdult, true)
                onSuccess.invoke()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        ageCheckDialog?.show()
    }

}