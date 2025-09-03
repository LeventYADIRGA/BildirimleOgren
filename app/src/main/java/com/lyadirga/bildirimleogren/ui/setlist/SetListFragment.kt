package com.lyadirga.bildirimleogren.ui.setlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.databinding.FragmentSetListBinding
import com.lyadirga.bildirimleogren.ui.MainViewModel
import com.lyadirga.bildirimleogren.ui.MarginItemDecoration
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import com.lyadirga.bildirimleogren.ui.setGone
import com.lyadirga.bildirimleogren.ui.setVisible
import com.lyadirga.bildirimleogren.ui.showToast
import com.lyadirga.bildirimleogren.util.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SetListFragment: BaseFragment<FragmentSetListBinding>() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: SetListAdapter

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetListBinding {
        return FragmentSetListBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {

        setupToolbar()

        adapter = SetListAdapter{
            val action = SetListFragmentDirections.actionSetListFragmentToSetDetailFragment(it.id, it.title)
            findNavController().navigate(action)
        }
        val verticalMargin = resources.getDimensionPixelSize(R.dimen.vertical_item_margin)
        val horizontalMargin = resources.getDimensionPixelSize(R.dimen.horizontal_item_margin)
        val dividerItemDecoration = MarginItemDecoration(verticalMargin, horizontalMargin)
        binding.listSets.addItemDecoration(dividerItemDecoration)
        binding.listSets.adapter = adapter

        val bottomSheet = SetEkleBottomSheet {
            viewModel.fetchSingleSheet(url = it)
        }
        binding.addButton.setOnClickListener {
            bottomSheet.show(childFragmentManager)
        }
    }

    override fun observeFlows() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.setAllSetSummariesFlow.collect {
                        if (it.isNotEmpty()) {
                            adapter.submitList(it)
                            binding.guideLayout.setGone()
                        } else {
                            binding.guideLayout.setVisible()
                        }
                    }
                }

                launch {
                    viewModel.errorEvent.collect { errorMessage ->
                        Toast.showAlertToast(requireActivity(), errorMessage)
                    }
                }

                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBar.isVisible = loading
                    }

                }
            }
        }
    } // end observeFlows


    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_bilgi -> {
                    val action =
                        SetListFragmentDirections.actionSetListFragmentToBilgiFragment()
                    findNavController().navigate(action)
                }

                R.id.action_settings -> {
                }

                R.id.action_share -> {
                    //shareAppLink()
                }

                R.id.action_rate_me -> {
                    //rateAppOnPlayStore()
                }
            }
            true
        }
    }

}