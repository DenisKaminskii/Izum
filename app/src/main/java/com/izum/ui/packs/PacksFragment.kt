package com.izum.ui.packs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.izum.databinding.FragmentPacksBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PacksFragment : Fragment(), CoroutineScope by MainScope() {

    companion object {

        private const val KEY_IS_OFFICIAL = "KEY_IS_OFFICIAL"

        fun newInstance(
            isOfficial: Boolean = true
        ): PacksFragment {
            val fragment = PacksFragment()
            fragment.arguments = Bundle().apply {
                putBoolean(KEY_IS_OFFICIAL, isOfficial)
            }
            return fragment
        }
    }

    private var _binding: FragmentPacksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PacksViewModel by lazy {
        ViewModelProvider(requireActivity())[PacksViewModel::class.java]
    }

    private val adapter = PacksAdapter { pack ->
        viewModel.onPackClick(pack)
    }

    private var isOfficial: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOfficial = arguments?.getBoolean(KEY_IS_OFFICIAL, true) ?: true
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
        binding.vPacks.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.vPacks.adapter = adapter
    }

    private fun update(state: PacksViewState) {
        when(state) {
            is PacksViewState.Packs -> {
                adapter.setItems(state.packs)
            }
            is PacksViewState.Loading -> {
                // show loading
            }
        }
    }

}