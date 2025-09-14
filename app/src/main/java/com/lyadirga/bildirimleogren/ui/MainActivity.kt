package com.lyadirga.bildirimleogren.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.databinding.ActivityMainBinding
import com.lyadirga.bildirimleogren.notification.NotificationWorker
import com.lyadirga.bildirimleogren.ui.base.BaseActivity
import com.lyadirga.bildirimleogren.util.Toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private val viewModel: MainViewModel by viewModels()


    @Inject
    lateinit var prefData: PrefData

    private val choices: Array<CharSequence> by lazy {
        resources.getStringArray(R.array.notification_intervals).map { it as CharSequence }.toTypedArray()
    }


    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1981
        const val UNIQUE_WORK_NAME = "notification_work"
        val intervalsInMinutes = arrayOf(30, 60, 180, 360, 1440, null)
    }


    override fun createBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Status bar color make tertiary
        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true)
        window.statusBarColor = typedValue.data

        initPermission()

        if(isInternetAvailable()){
            viewModel.fetchSheetsFromDbUrls()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 🇹🇷Türkçe: İzin verildi
                // 🇬🇧English: Permission granted
                openNotificationIntervalSettings()

            } else {
                // 🇹🇷Türkçe: İzin reddedildi
                // 🇬🇧English: Permission denied
                showNotificationPermissionDialog()
            }
        }
    }

    fun openNotificationIntervalSettings() {

        lifecycleScope.launch {
            var currentIntervalIndex = prefData.getNotificationIntervalIndexOnce()
            val oldIndex = currentIntervalIndex

            val builder = MaterialAlertDialogBuilder(this@MainActivity, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
                setTitle(R.string.notification_interval_title)
                setPositiveButton(R.string.generic_ok) { _, _ ->
                    lifecycleScope.launch {
                        if (currentIntervalIndex != oldIndex) {
                            prefData.setNotificationIntervalIndex(currentIntervalIndex)
                                val enabledSets = prefData.getNotificationSetIdsOnce()
                                viewModel.getAllSetSummariesOnce { summaries ->
                                    if (summaries.isEmpty()){
                                        showAlert(R.string.notification_no_sets_message)
                                    }
                                    else if (enabledSets.isEmpty()) {
                                        showAlert(R.string.notification_no_enabled_sets_message)
                                    } else {
                                        val notificationInterval = intervalsInMinutes[currentIntervalIndex]
                                        scheduleNotifications(notificationInterval, choices[currentIntervalIndex])
                                    }
                                }
                        }

                    }
                }
                setSingleChoiceItems(choices, currentIntervalIndex) { _, which ->
                    currentIntervalIndex = which
                }
            }

            val dialog = builder.create()
            dialog.show()

        }
    }

     private fun scheduleNotifications(notificationInterval: Int?, intervalLabel: CharSequence) {

        val workManager = WorkManager.getInstance(this)
        notificationInterval?.let {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(it.toLong(), TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // Cancel previous if any and replace with the new one
                workRequest
            )
            showToast(getString(R.string.notification_scheduled, intervalLabel))

        } ?:run {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            Toast.showSuccessToast(this, R.string.notification_all_disabled)
        }

    }

    fun scheduleNotificationsFromSetDetail(notificationInterval: Int?) {

        val workManager = WorkManager.getInstance(this)
        notificationInterval?.let {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(it.toLong(), TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // Cancel previous if any and replace with the new one
                workRequest
            )

        } ?:run {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            Toast.showSuccessToast(this, R.string.notifications_disabled_no_enabled_set)
        }

    }


    private fun initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // İzni daha önce istenmiş ve reddedilmiş mi?
            val isPermissionDenied = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_DENIED && ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (isPermissionDenied) {
                showNotificationPermissionDialog()
            } else if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun showNotificationPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
            setTitle(R.string.notification_permission_title)
            setMessage(R.string.notification_permission_message)
            setPositiveButton(R.string.go_to_settings) { _, _ ->
                openNotificationSettings()
            }.setCancelable(false).show()
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

}