package com.lyadirga.bildirimleogren.ui.setdetail

import android.os.Bundle
import android.util.Log.d
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
                requireContext().showToast("Set bulunamadı")
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

                        updateNotification()

                        requireContext().showToast(
                            if (isEnabled) "Bildirim açıldı"
                            else "Bu set için bildirim kapatıldı"
                        )
                    }

                    true
                }

                R.id.delete -> {
                    MaterialAlertDialogBuilder(
                        requireContext(),
                        R.style.Theme_BildirimleOgren_MaterialAlertDialog
                    )
                        .setTitle("Seti Sil")
                        .setMessage("Bu seti silmek istediğinizden emin misiniz?")
                        .setPositiveButton("Evet") { _, _ ->
                            lifecycleScope.launch {
                                // Seti sil
                                viewModel.deleteSet(args.setId)

                                // Eğer bildirim açıksa kaldır
                                val enabledSets = prefData.getNotificationSetIdsOnce().toMutableSet()
                                if (args.setId in enabledSets) {
                                    enabledSets.remove(args.setId)
                                    prefData.saveNotificationSetIds(enabledSets.toList())
                                }

                                requireContext().showToast("Set silindi")
                                findNavController().popBackStack() // Listeye dön
                            }
                        }
                        .setNegativeButton("Hayır", null)
                        .show()
                    true
                }
                else -> false
            }
        }
    }

    private fun updateNotification(){
        lifecycleScope.launch {
            val intervalIndex = prefData.getNotificationIntervalIndexOnce()
            val enabledSets = prefData.getNotificationSetIdsOnce()
            val activity = requireActivity() as MainActivity
            if (intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.isNotEmpty()) {
                val notificationInterval  = MainActivity.intervalsInMinutes[intervalIndex]
                activity.scheduleNotificationsFromSetDetail(notificationInterval)
            }else if (intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX){
                activity.scheduleNotificationsFromSetDetail(null) // Bildirim kapat, çünkü enabledSets boş
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.releaseTextToSpeech()
    }

}