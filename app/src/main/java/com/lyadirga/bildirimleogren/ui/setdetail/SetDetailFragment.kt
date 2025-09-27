package com.lyadirga.bildirimleogren.ui.setdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.databinding.FragmentSetDetailBinding
import com.lyadirga.bildirimleogren.ui.MainActivity
import com.lyadirga.bildirimleogren.ui.MainViewModel
import com.lyadirga.bildirimleogren.ui.base.BaseFragment
import com.lyadirga.bildirimleogren.ui.showToast
import com.lyadirga.bildirimleogren.util.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SetDetailFragment : BaseFragment<FragmentSetDetailBinding>() {

    private val viewModel: MainViewModel by activityViewModels()
    private val args: SetDetailFragmentArgs by navArgs() // Safe Args
    private lateinit var listAdapter: SetItemsAdapter

    @Inject
    lateinit var prefData: PrefData


    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSetDetailBinding {
        return FragmentSetDetailBinding.inflate(inflater, container, false)
    }

    override fun prepareView(savedInstanceState: Bundle?) {

        setupToolbar()
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
                requireContext().showToast(R.string.set_not_found)
            }
        }
    }

    private fun setupToolbar() {

        lifecycleScope.launch {
            val enabledSets = prefData.getNotificationSetIdsOnce()
            val isEnabled = args.setId in enabledSets
            binding.toolbar.menu.findItem(R.id.notification)?.setIcon(
                if (isEnabled) R.drawable.notification_enable
                else R.drawable.notification_disable
            )
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.notification -> {
                    lifecycleScope.launch {
                        prefData.toggleNotificationSetId(args.setId)
                        val enabledSets = prefData.getNotificationSetIdsOnce()
                        val isEnabled = args.setId in enabledSets

                        it.setIcon(
                            if (isEnabled) R.drawable.notification_enable
                            else R.drawable.notification_disable
                        )

                        updateNotification(isEnabled, enabledSets)

                    }

                    true
                }

                R.id.delete -> {
                    MaterialAlertDialogBuilder(
                        requireContext()
                    )
                        .setTitle(R.string.delete_set_title)
                        .setMessage(R.string.delete_set_message)
                        .setPositiveButton(R.string.generic_yes) { _, _ ->
                            lifecycleScope.launch {
                                // delete set
                                viewModel.deleteSet(args.setId)

                                // 🇹🇷Türkçe: Eğer bildirim açıksa kaldır
                                // 🇬🇧English: Remove if notification is enabled
                                val enabledSets = prefData.getNotificationSetIdsOnce().toMutableSet()
                                if (args.setId in enabledSets) {
                                    enabledSets.remove(args.setId)
                                    prefData.saveNotificationSetIds(enabledSets.toList())
                                }

                                requireContext().showToast(R.string.set_deleted)
                                findNavController().popBackStack() // Go back to the list
                            }
                        }
                        .setNegativeButton(R.string.generic_no, null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateNotification(isEnabled: Boolean, enabledSets: List<Long>) {
        lifecycleScope.launch {
            val intervalIndex = prefData.getNotificationIntervalIndexOnce()
            val activity = requireActivity() as MainActivity
            if (isEnabled && intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1) {
                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor. Bildirimi başlat
                // 🇬🇧English: When no set has notifications enabled, this set will be enabled. Start the notification.
                val notificationInterval  = MainActivity.intervalsInMinutes[intervalIndex]
                activity.scheduleNotificationsFromSetDetail(notificationInterval)
                Toast.showSuccessToast(requireActivity(), R.string.notification_set_enabled)
            } else if (isEnabled && intervalIndex == PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1){
                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor ama bildirim sıklığı ayarlarından seçim yapılmamış. Bildirim sıklığı dialog unu aç.
                // 🇬🇧English: When no set has notifications enabled, this set is enabled but no frequency is selected. Open the notification frequency dialog.
                activity.openNotificationIntervalSettings()
            }
            else if (intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.isEmpty()){
                // 🇹🇷Türkçe: Bildirim kapat, çünkü enabledSets boş
                // 🇬🇧English: Turn off notification because enabledSets is empty
                activity.scheduleNotificationsFromSetDetail(null)
                prefData.resetIndex()
            }else if (isEnabled.not()){
                Toast.showSuccessToast(requireActivity(), R.string.notification_set_disabled)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.releaseTextToSpeech()
    }

}