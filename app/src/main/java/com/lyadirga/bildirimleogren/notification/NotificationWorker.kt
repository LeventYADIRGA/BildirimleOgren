package com.lyadirga.bildirimleogren.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.AppDatabase
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.Repository
import com.lyadirga.bildirimleogren.ui.MainActivity

class NotificationWorker (
     appContext: Context,
     params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }
    private val repository: Repository by lazy {
        Repository(database.languageDao())
    }
    private val prefData: PrefData by lazy {
        PrefData(appContext)
    }

    override suspend fun doWork(): Result {

        val enabledSetIds = prefData.getNotificationSetIdsOnce()
        if (enabledSetIds.isEmpty()) return Result.success()

        // Room’dan sadece aktif setleri al
        // Get only active sets from Room
        val activeSets = repository.getSetsByIds(enabledSetIds)
        if (activeSets.isEmpty()) return Result.success()

        // Tüm aktif setlerdeki öğeleri tek bir listede birleştir
        // Combine items from all active sets into a single list
        val allItems = activeSets.flatMap { it.items }
        if (allItems.isEmpty()) return Result.success()

        // Index güncelleme
        // Update index
        var index = prefData.getIndexOnce()
        index++
        if (index >= allItems.size) {
            index = 0
        }
        prefData.setIndex(index)

        val item = allItems[index]
        showNotification(item.wordOrSentence, item.meaning)

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminder_channel"

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channel = NotificationChannel(
            channelId,
            "Reminder Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(soundUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build())
        }

        notificationManager.createNotificationChannel(channel)

        // Ana aktiviteyi açmak için bir intent
        // An intent to open the main activity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Automatically dismiss when the notification is clicked
            .setSmallIcon(R.mipmap.app_icon)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}