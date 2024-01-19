package com.lyadirga.bildirimleogren

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getData
import com.lyadirga.bildirimleogren.data.week1
import com.lyadirga.bildirimleogren.util.ParseCsv

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: RecyclerAdapter
    private lateinit var list: RecyclerView
    private lateinit var title: MaterialTextView
    private lateinit var prefData: PrefData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefData = PrefData(this)

        title = findViewById<MaterialTextView>(R.id.title)
        title.text = resources.getStringArray(R.array.calisma_setleri)[prefData.getCalismaSeti()]
        list = findViewById<RecyclerView>(R.id.list)
        val animation =
            AnimationUtils.loadAnimation(this, R.anim.layout_animation_fall_down)
        list.layoutAnimation = LayoutAnimationController(animation)
        adapter = RecyclerAdapter(this, week1)
        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        list.addItemDecoration(dividerItemDecoration)
        list.adapter = adapter
        list.scheduleLayoutAnimation()

        ParseCsv.parse(this)
    }

    override fun onResume() {
        super.onResume()
        if (isNotificationPermissionGranted()) {
            NotificationWorker.startPeriodicWork()
        } else {
            // Bildirim izni yok, kullanıcıyı izin vermeye davet et veya başka bir işlem yap
            showNotificationPermissionDialog()
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
                    adapter.swapData(data)
                    list.scheduleLayoutAnimation()
            }
            .setSingleChoiceItems(choices, checkedItem){_, which ->
                checkedItem = which
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bildirim İzin")
            .setMessage("Uygulama bildirim göndermek istiyor. Lütfen izin verin.")
            .setPositiveButton("Ayarlar'a Git", DialogInterface.OnClickListener { _, _ ->
                openNotificationSettings()
            })
            .setNegativeButton("İptal", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
                // İptal durumunda gerekli işlemleri yapabilirsiniz.
            })
            .show()
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.releaseTextToSpeech()
    }
}