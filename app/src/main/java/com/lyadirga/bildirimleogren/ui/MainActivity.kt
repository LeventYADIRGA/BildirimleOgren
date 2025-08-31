package com.lyadirga.bildirimleogren.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.textview.MaterialTextView
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getLanguageSet
import com.lyadirga.bildirimleogren.data.languageSets
import com.lyadirga.bildirimleogren.notification.NotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var adapter: RecyclerAdapter? = null
    private lateinit var list: RecyclerView
    private lateinit var title: MaterialTextView
    private lateinit var prefData: PrefData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Android 15 ve üzeri için
        val rootView = findViewById<View>(android.R.id.content)
        setupEdgeToEdgeAndStatusBar(rootView)

        prefData = PrefData(this)

        initPermission()

        title = findViewById(R.id.title)
        list = findViewById(R.id.list)

        val currentCalismaSetiIndex = prefData.getCalismaSeti()
        val currentCalismaSeti = getLanguageSet(currentCalismaSetiIndex)
        title.text = currentCalismaSeti?.title ?: ""

        val animation =
            AnimationUtils.loadAnimation(this, R.anim.layout_animation_fall_down)
        list.layoutAnimation = LayoutAnimationController(animation)
        adapter = RecyclerAdapter(this, currentCalismaSeti?.items ?: emptyList())
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)
        list.adapter = adapter
        list.scheduleLayoutAnimation()

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
                    prefData.setIndex(0)
                    title.text = choices[currentCalismaSetiIndex]
                    val currentCalismaSeti = getLanguageSet(currentCalismaSetiIndex)
                    adapter?.swapData(currentCalismaSeti?.items ?: emptyList())
                    list.scheduleLayoutAnimation()
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

    private fun setupEdgeToEdgeAndStatusBar(view: View) {
        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
                    v.paddingLeft,
                    systemBarsInsets.top,
                    v.paddingRight,
                    systemBarsInsets.bottom
                )

                val isDarkTheme = when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> true   // Koyu mod
                    Configuration.UI_MODE_NIGHT_NO -> false   // Açık mod
                    else -> false
                }

                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

                // true -> ikonlar koyu, false -> ikonlar açık
                windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme


                insets
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adapter?.releaseTextToSpeech()
        adapter = null
    }
}