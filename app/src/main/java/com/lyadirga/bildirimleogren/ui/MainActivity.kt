package com.lyadirga.bildirimleogren.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getLanguageSet
import com.lyadirga.bildirimleogren.data.languageSets
import com.lyadirga.bildirimleogren.data.remote.SHEET_URL1
import com.lyadirga.bildirimleogren.data.remote.SHEET_URL2
import com.lyadirga.bildirimleogren.databinding.ActivityMainBinding
import com.lyadirga.bildirimleogren.notification.NotificationWorker
import com.lyadirga.bildirimleogren.ui.base.BaseActivity
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()


    private lateinit var listAdapter: LanguageListAdapter
    private lateinit var prefData: PrefData

    override fun createBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun prepareView(savedInstanceState: Bundle?) {

        prefData = PrefData(this)
        setSupportActionBar(binding.toolbar) // MaterialToolbar’ı ActionBar olarak ayarla

        initPermission()
        fetchAllSheets()

        val currentCalismaSetiIndex = prefData.getCalismaSeti()
        var currentCalismaSeti = getLanguageSet(currentCalismaSetiIndex)
        if (currentCalismaSetiIndex >= 100){
            val sets = prefData.getLanguageSets()
            if (sets.isNotEmpty()){
                currentCalismaSeti = sets[currentCalismaSetiIndex - 100]
            }
        }
        binding.title.text = currentCalismaSeti?.title ?: ""

        listAdapter = LanguageListAdapter(this)
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)

        binding.list.apply {
            addItemDecoration(dividerItemDecoration)
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            scheduleLayoutAnimation()
        }
        listAdapter.submitList(currentCalismaSeti?.items)
    }

    override fun observeViewModel() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.languageSets.collect {
                        prefData.saveLanguageSets(it)
                    }
                }

                launch {
                    viewModel.errorEvent.collect { errorMessage ->
                        showToast(errorMessage)
                    }
                }
            }
        }
    }


    private fun fetchAllSheets() {
        val urls = listOf(SHEET_URL1, SHEET_URL2)
        if (isInternetAvailable()) {
            viewModel.fetchSheets(urls)
        } else {
            showToast("İnternet yok")
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
                    1
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                if(prefData.isFirstLaunch()){
                    scheduleNotifications(15)
                    prefData.setFirstLaunch(false)
                }
            } else {
                // İzin reddedildi
                showNotificationPermissionDialog()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_local -> {
                openSelectionDialog()
                true
            }
            R.id.action_remote -> {
                openRemoteSetsSelectionDialog()
                true
            }
            R.id.action_settings -> {
                openNotificationIntervalSettings()
                true
            }

            R.id.action_share -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openNotificationIntervalSettings() {

        // Seçenekler
        val choices: Array<CharSequence> = arrayOf("30 dakika", "1 saat", "6 saat", "1 gün")
        val intervalsInMinutes = arrayOf(30, 60, 360, 1440) // dakika cinsinden

        var currentIntervalIndex = prefData.getNotificationIntervalIndex()
        MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog)

        val builder = MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
            setTitle("Bildirim Sıklığı")
            setPositiveButton("Tamam") { _, _ ->
                prefData.setNotificationIntervalIndex(currentIntervalIndex)
                val notificationInterval = intervalsInMinutes[currentIntervalIndex]
                scheduleNotifications(notificationInterval)
            }
            setSingleChoiceItems(choices, currentIntervalIndex) { _, which ->
                currentIntervalIndex = which
            }
        }

        val dialog = builder.create()
        dialog.show()

    }

    private fun openRemoteSetsSelectionDialog() {

        val languageSets = prefData.getLanguageSets()
        if (languageSets.isEmpty()){
            showToast("Birkaç saniye sonra tekrar deneyiniz.", Toast.LENGTH_LONG)
            return
        }
        val choices: Array<CharSequence> = languageSets
            .map { it.title as CharSequence }
            .toTypedArray()

        val calismaSetiIndex = prefData.getCalismaSeti()

        var currentCalismaSetiIndex = 0
        //100 ve 100 den büyükse remote
        if (calismaSetiIndex >= 100){
            currentCalismaSetiIndex = calismaSetiIndex - 100
        }

        val builder = MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
            setTitle("E Tablolardan Çalışma Seti Seçin")
            setPositiveButton("Tamam") { _, _ ->
                binding.title.text = choices[currentCalismaSetiIndex]
                prefData.setCalismaSeti(100 + currentCalismaSetiIndex)
                listAdapter.submitList(languageSets[currentCalismaSetiIndex].items)
            }
                .setSingleChoiceItems(choices, currentCalismaSetiIndex) { _, which ->
                    currentCalismaSetiIndex = which
                }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun openSelectionDialog() {

        val choices: Array<CharSequence> = languageSets
            .map { it.title as CharSequence }
            .toTypedArray()

        var currentCalismaSetiIndex = prefData.getCalismaSeti()
        if (currentCalismaSetiIndex >= 100){
            currentCalismaSetiIndex = 0
        }

        val builder = MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
            setTitle("Çalışma Seti Seçin")
            setPositiveButton("Tamam"){ _, _ ->
                    prefData.setCalismaSeti(currentCalismaSetiIndex)
                    prefData.resetIndex()
                    binding.title.text = choices[currentCalismaSetiIndex]
                    val currentCalismaSeti = getLanguageSet(currentCalismaSetiIndex)
                    listAdapter.submitList(currentCalismaSeti?.items)
            }
            .setSingleChoiceItems(choices, currentCalismaSetiIndex){_, which ->
                currentCalismaSetiIndex = which
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
    
    private fun showNotificationPermissionDialog() {
        MaterialAlertDialogBuilder(this, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
            setTitle("Bildirim İzin")
            setMessage("Uygulamamızın temel özelliği bildirim göndermesidir. Lütfen izin verin.")
            setPositiveButton("Ayarlar'a Git") { _, _ ->
                openNotificationSettings()
            }.setCancelable(false).show()
        }
    }

    private fun scheduleNotifications(notificationInterval: Int) {

        val intervalMinutes = notificationInterval.coerceAtLeast(15)
        // WorkManager minimum 15 dakikada bir çalışır, daha küçükse 15 yap

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notification_work",
            ExistingPeriodicWorkPolicy.REPLACE, // Önceki varsa iptal et ve yenisiyle değiştir
            workRequest
        )
    }


    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        listAdapter.releaseTextToSpeech()
    }
}