package com.pickone.ui.packs

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
import com.pickone.R
import com.pickone.databinding.FragmentPacksBinding
import com.pickone.domain.core.PreferenceCache
import com.pickone.domain.core.PreferenceKey
import com.pickone.ui.KEY_ARGS_INPUT
import com.pickone.ui.custom.AddCustomPackDialog
import com.pickone.ui.dp
import com.pickone.ui.utils.RecyclerGridItemDecoration
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

    override fun onStop() {
        super.onStop()
        addCustomPackDialog?.dismissAllowingStateLoss()
        addCustomPackDialog = null
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

    private fun initView() = with(binding) {
        rvPacks.layoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    when (adapter.getItemViewType(position)) {
                        PacksAdapter.ViewType.HEADER.ordinal -> 2
                        else -> 1
                    }
            }
        }
        rvPacks.adapter = adapter
        rvPacks.addItemDecoration(RecyclerGridItemDecoration(vertical = requireContext().dp(8)))

        tvCreatePack.setOnClickListener { onCreatePackClick?.invoke() }

        fabAddCustomPack.isVisible = isCustom
        fabAddCustomPack.setOnClickListener { onAddCustomPackClicked() }
    }

    private fun update(state: PacksViewState) {
        val isLoadingVisible =
            if (isCustom) state.customPacks == null
            else state.publicPacks == null

        val isNoPacksVisible =
            if (isCustom) state.customPacks?.isEmpty() == true
            else state.publicPacks?.isEmpty() == true

        val isPacksVisible =
            if (isCustom) state.customPacks?.isNotEmpty() == true
            else state.publicPacks?.isNotEmpty() == true

        binding.vProgress.isVisible = isLoadingVisible
        binding.vgNoPacks.isVisible = isNoPacksVisible
        binding.rvPacks.isVisible = isPacksVisible

        if (!isPacksVisible) return

        val items = mutableListOf<PacksItem>()

        if (!isCustom) {
            items.addAll(state.publicPacks?.map {
                PacksItem.Pack(
                    pack = it,
                    description = it.description ?: "",
                    hasSubscription = state.hasSubscription,
                    title = it.title,
                    gradientStartColor = it.gradientStartColor,
                    gradientEndColor = it.gradientEndColor,
                    contentColor = it.contentColor,
                    pollsCount = it.pollsCount,
                    isPaid = it.isPaid,
                    isMine = false
                )
            } ?: emptyList())
        } else {
            val customPacks = state.customPacks?.map {
                PacksItem.Pack(
                    pack = it,
                    description = it.description ?: "",
                    hasSubscription = state.hasSubscription,
                    title = it.title,
                    gradientStartColor =
                    if (it.isMine) requireContext().getColor(R.color.black_gradient_start)
                    else requireContext().getColor(R.color.white_gradient_start),
                    gradientEndColor =
                    if (it.isMine) requireContext().getColor(R.color.black_gradient_end)
                    else requireContext().getColor(R.color.white_gradient_end),
                    contentColor =
                    if (it.isMine) requireContext().getColor(R.color.white)
                    else requireContext().getColor(R.color.black_soft),
                    pollsCount = it.pollsCount,
                    isPaid = false,
                    isMine = it.isMine
                )
            } ?: emptyList()

            val myPacks = customPacks.filter { it.isMine }
            val addedPacks = customPacks.filter { !it.isMine }

            if (myPacks.isNotEmpty()) {
                items.add(PacksItem.Header("Created"))
                items.addAll(myPacks)
            }

            if (addedPacks.isNotEmpty()) {
                items.add(PacksItem.Header("Added"))
                items.addAll(addedPacks)
            }
        }

        adapter.setItems(items)

    }

    private var ageCheckDialog: AlertDialog? = null

    private fun checkAge(
        onSuccess: () -> Unit
    ) {
        val isAdult = preferenceCache.getBoolean(PreferenceKey.IsAdult.name, false)
        if (isAdult) {
            onSuccess()
            return
        }

        ageCheckDialog?.hide()
        ageCheckDialog = null
        ageCheckDialog = AlertDialog.Builder(requireContext(), R.style.RoundedDialog)
            .setTitle("Are you 18 years old or above?")
            .setMessage("Content contains obscene language and is not suitable for kids.")
            .setPositiveButton("Yes") { dialog, _ ->
                preferenceCache.putBoolean(PreferenceKey.IsAdult.name, true)
                onSuccess.invoke()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        ageCheckDialog?.show()
    }

    private var addCustomPackDialog: AddCustomPackDialog? = null

    private fun onAddCustomPackClicked() {
        addCustomPackDialog?.dismissAllowingStateLoss()
        addCustomPackDialog = null
        addCustomPackDialog = AddCustomPackDialog.getInstance(
            onStart = { packId ->
                viewModel.onNewCustomPackStartClick(packId)
            }
        )
        addCustomPackDialog?.show(childFragmentManager, "PackTitleEditDialog")
    }

}