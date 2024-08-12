package com.lyadirga.bildirimleogren

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getData
import com.lyadirga.bildirimleogren.data.week1
import com.lyadirga.bildirimleogren.util.ParseCsv

class MainActivity : AppCompatActivity() {

    private var adapter: RecyclerAdapter? = null
    private lateinit var list: RecyclerView
    private lateinit var title: MaterialTextView
    private lateinit var prefData: PrefData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()

        prefData = PrefData(this)

        title = findViewById(R.id.title)
        list = findViewById(R.id.list)

        val currentCalismaSetiIndex = prefData.getCalismaSeti()
        val currentCalismaSeti = getData(currentCalismaSetiIndex)
        title.text = resources.getStringArray(R.array.calisma_setleri)[prefData.getCalismaSeti()]

        val animation =
            AnimationUtils.loadAnimation(this, R.anim.layout_animation_fall_down)
        list.layoutAnimation = LayoutAnimationController(animation)
        adapter = RecyclerAdapter(this, currentCalismaSeti)
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)
        list.adapter = adapter
        list.scheduleLayoutAnimation()

        NotificationWorker.startPeriodicWork()
        //ParseCsv.parse(this)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildi
                NotificationWorker.startPeriodicWork()
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
        var checkedItem = prefData.getCalismaSeti()
        val choices = resources.getStringArray(R.array.calisma_setleri)
        val builder = AlertDialog.Builder(this).apply {
            setTitle("Çalışma Seti Seçin")
            setPositiveButton("Tamam"){ _, _ ->
                    prefData.setCalismaSeti(checkedItem)
                    prefData.setIndex(0)
                    title.text = resources.getStringArray(R.array.calisma_setleri)[checkedItem]
                    val data = getData(checkedItem)
                    adapter?.swapData(data)
                    list.scheduleLayoutAnimation()
            }
            .setSingleChoiceItems(choices, checkedItem){_, which ->
                checkedItem = which
            }
        }

        val dialog = builder.create()
        dialog.show()
    }
    
    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bildirim İzin")
            .setMessage("Uygulamamızın temel özelliği bildirim göndermesidir. Lütfen izin verin.")
            .setPositiveButton("Ayarlar'a Git") { _, _ ->
                openNotificationSettings()
            }.setCancelable(false).show()
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.releaseTextToSpeech()
        adapter = null
    }
}