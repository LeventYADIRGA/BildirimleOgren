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
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getLanguageSet
import com.lyadirga.bildirimleogren.data.languageSets
import com.lyadirga.bildirimleogren.data.remote.SHEET_URL
import com.lyadirga.bildirimleogren.data.remote.SHEET_URL2
import com.lyadirga.bildirimleogren.data.remote.SSS
import com.lyadirga.bildirimleogren.databinding.ActivityMainBinding
import com.lyadirga.bildirimleogren.notification.NotificationWorker
import com.lyadirga.bildirimleogren.ui.base.BaseActivity
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()


    private var listAdapter: RecyclerAdapter? = null
    private lateinit var prefData: PrefData

    override fun createBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }

    override fun prepareView(savedInstanceState: Bundle?) {

        prefData = PrefData(this)

        initPermission()
        test()

        val currentCalismaSetiIndex = prefData.getCalismaSeti()
        val currentCalismaSeti = getLanguageSet(currentCalismaSetiIndex)
        binding.title.text = currentCalismaSeti?.title ?: ""

        val animation =
            AnimationUtils.loadAnimation(this, R.anim.layout_animation_fall_down)
        listAdapter = RecyclerAdapter(this, currentCalismaSeti?.items ?: emptyList())
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        binding.list.apply {
            layoutAnimation = LayoutAnimationController(animation)
            addItemDecoration(dividerItemDecoration)
            adapter = listAdapter
            scheduleLayoutAnimation()
        }
    }

    override fun observeViewModel() {}


    private fun test() {
        viewModel.fetchSheet(SSS)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.wordSet.collect { wordSet ->
                        // UI güncelle
                        println("LLL: $wordSet")
                    }
                }

                launch {
                    viewModel.errorEvent.collect { errorMessage ->
                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                    scheduleNotificationWork()
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
            R.id.action_settings -> {
                openSelectionDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openSelectionDialog() {

        val choices: Array<CharSequence> = languageSets
            .map { it.title as CharSequence }
            .toTypedArray()

        var currentCalismaSetiIndex = prefData.getCalismaSeti()

        val builder = AlertDialog.Builder(this).apply {
            setTitle("Çalışma Seti Seçin")
            setPositiveButton("Tamam"){ _, _ ->
                    prefData.setCalismaSeti(currentCalismaSetiIndex)
                    prefData.resetIndex()
                    binding.title.text = choices[currentCalismaSetiIndex]
                    val currentCalismaSeti = getLanguageSet(currentCalismaSetiIndex)
                    listAdapter?.swapData(currentCalismaSeti?.items ?: emptyList())
                    binding.list.scheduleLayoutAnimation()
            }
            .setSingleChoiceItems(choices, currentCalismaSetiIndex){_, which ->
                currentCalismaSetiIndex = which
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
    
    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Bildirim İzin")
            setMessage("Uygulamamızın temel özelliği bildirim göndermesidir. Lütfen izin verin.")
            setPositiveButton("Ayarlar'a Git") { _, _ ->
                openNotificationSettings()
            }.setCancelable(false).show()
        }
    }

    private fun scheduleNotificationWork(){
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notification_work",
            ExistingPeriodicWorkPolicy.REPLACE,
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
        listAdapter?.releaseTextToSpeech()
        listAdapter = null
    }
}