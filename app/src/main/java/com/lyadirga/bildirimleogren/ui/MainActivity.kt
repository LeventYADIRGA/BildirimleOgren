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

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1981
        private val choices: Array<CharSequence> = arrayOf("30 dakika", "1 saat", "3 saat", "6 saat", "1 gün", "Şimdilik kapalı kalsın")
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

        // Status bar rengini secondary yap
        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true)
        window.statusBarColor = typedValue.data

        initPermission()
        viewModel.fetchSheetsFromDbUrls()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                openNotificationIntervalSettings()

            } else {
                // İzin reddedildi
                showNotificationPermissionDialog()
            }
        }
    }

    fun openNotificationIntervalSettings() {

        lifecycleScope.launch {
            var currentIntervalIndex = prefData.getNotificationIntervalIndexOnce()
            val oldIndex = currentIntervalIndex

            val builder = MaterialAlertDialogBuilder(this@MainActivity, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
                setTitle("Bildirim Sıklığı")
                setPositiveButton("Tamam") { _, _ ->
                    lifecycleScope.launch {
                        if (currentIntervalIndex != oldIndex) {
                            prefData.setNotificationIntervalIndex(currentIntervalIndex)
                                val enabledSets = prefData.getNotificationSetIdsOnce()
                                viewModel.getAllSetSummariesOnce { summaries ->
                                    if (summaries.isEmpty()){
                                        showAlert("Henüz hiç Çalışma Seti eklemediniz. Çalışma setleri eklediğinizde istediğiniz setleri bildirim olarak ayarlayabilirsiniz.")
                                    }
                                    else if (enabledSets.isEmpty()) {
                                        showAlert("Bildirimler süresi ayarlandı, ama herhangi bir set için bildirim ayarlanmadı. Set detayına gittikten sonra üstteki bildirim ikonu ile set için bildirimi aktif ediniz.")
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
                "notification_work",
                ExistingPeriodicWorkPolicy.REPLACE, // Önceki varsa iptal et ve yenisiyle değiştir
                workRequest
            )
            showToast("Bildirimler $intervalLabel olarak ayarlandı")

        } ?:run {
            workManager.cancelUniqueWork("notification_work")
            this.showToast("Bildirimler kapatıldı")
        }

    }

    fun scheduleNotificationsFromSetDetail(notificationInterval: Int?) {

        val workManager = WorkManager.getInstance(this)
        notificationInterval?.let {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(it.toLong(), TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "notification_work",
                ExistingPeriodicWorkPolicy.REPLACE, // Önceki varsa iptal et ve yenisiyle değiştir
                workRequest
            )

        } ?:run {
            workManager.cancelUniqueWork("notification_work")
            this.showToast("Bildirim için hiç çalışma seti bulunmuyor")
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
            setTitle("Bildirim İzni")
            setMessage("Uygulamamızın temel özelliği bildirim göndermesidir. Lütfen izin verin.")
            setPositiveButton("Ayarlar'a Git") { _, _ ->
                openNotificationSettings()
            }.setCancelable(false).show()
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    override fun observeFlows() {}
}