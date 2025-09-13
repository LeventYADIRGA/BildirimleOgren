package com.lyadirga.bildirimleogren.ui.setlist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.databinding.FragmentSetListBinding
import com.lyadirga.bildirimleogren.ui.MainActivity
import com.lyadirga.bildirimleogren.ui.MainViewModel
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import com.lyadirga.bildirimleogren.ui.setGone
import com.lyadirga.bildirimleogren.ui.setVisible
import com.lyadirga.bildirimleogren.util.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetListFragment: BaseFragment<FragmentSetListBinding>() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: SetListAdapter

    @Inject
    lateinit var prefData: PrefData

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetListBinding {
        return FragmentSetListBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {

        setupToolbar()

        adapter = SetListAdapter {
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

                // 🇹🇷Türkçe: PrefData'dan notification açık olan set id'lerini izle
                // 🇬🇧English: Observe the set ids that have notifications turned on from PrefData
                launch {
                    prefData.observeNotificationSetIds().collect { enabledSetIds ->
                        // 🇹🇷Türkçe: adapter'e bildir
                        // 🇬🇧English: notify adapter
                        adapter.updateEnabledSets(enabledSetIds.toSet())
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
                    val activity = requireActivity() as MainActivity
                    activity.openNotificationIntervalSettings()
                }

                R.id.action_share -> {
                    shareAppLink()
                }

                R.id.action_rate_me -> {
                    rateAppOnPlayStore()
                }
            }
            true
        }
    }

    private fun shareAppLink() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=${packageName}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
    }

    private fun rateAppOnPlayStore() {
        val uri = "market://details?id=$packageName".toUri()
        val goToMarket = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            // 🇹🇷Türkçe: Play Store yoksa tarayıcı ile aç
            // 🇬🇧English: If Play Store is not available, open in browser
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$packageName".toUri()
                )
            )
        }
    }

    private val packageName: String
        get() = "com.lyadirga.bildirimleogren"
}