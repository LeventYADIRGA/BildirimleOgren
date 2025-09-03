package com.lyadirga.bildirimleogren.ui.setdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lyadirga.bildirimleogren.databinding.FragmentSetDetailBinding
import com.lyadirga.bildirimleogren.ui.LanguageListAdapter
import com.lyadirga.bildirimleogren.ui.MainViewModel
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import com.lyadirga.bildirimleogren.ui.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SetDetailFragment: BaseFragment<FragmentSetDetailBinding>() {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: SetDetailFragmentArgs by navArgs() // Safe Args
    private lateinit var listAdapter: SetItemsAdapter


    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetDetailBinding {
        return FragmentSetDetailBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.toolbar.apply {
            title = args.title
            setupWithNavController(navController, appBarConfiguration)
        }

        listAdapter = SetItemsAdapter(requireContext())
        val dividerItemDecoration =
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)

        binding.list.apply {
            addItemDecoration(dividerItemDecoration)
            adapter = listAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun observeFlows() {
        viewModel.getSetDetails(args.setId) { setDetails ->
            if (setDetails != null) {
                listAdapter.submitList(setDetails.items)
            } else {
                requireContext().showToast("Set bulunamadı")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.releaseTextToSpeech()
    }

}