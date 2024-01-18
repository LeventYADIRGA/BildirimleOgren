package com.lyadirga.bildirimleogren

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Random
import java.util.concurrent.TimeUnit

class NotificationWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {

        val prefData = PrefData(context)
        var index = prefData.getIndex()
        val setIndex = prefData.getCalismaSeti()
        val set = getData(setIndex)
        index += 1
        if (index >= set.size) {
            index = 0
        }
        prefData.setIndex(index)


        // bildirim gönder
        val model = week1[index]
        sendNotification(model.wordOrSentence, model.meaning)

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "channel_id",
            "Bildirimle Ogren",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // Bildirim oluşturun
        val notification = NotificationCompat.Builder(applicationContext, "channel_id")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_spellcheck_24)
            .build()

        // Bildirimi gönderin
        notificationManager.notify(Random().nextInt(), notification)
    }

    companion object {
        private const val WORK_NAME = "NotificationWorker"

        fun startPeriodicWork() {
            val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                30, // 30 dakika
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }
}
