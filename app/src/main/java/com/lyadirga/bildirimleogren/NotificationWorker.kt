package com.lyadirga.bildirimleogren

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getData
import com.lyadirga.bildirimleogren.data.week1
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
        val model =set[index]
        sendNotification(model.wordOrSentence, model.meaning)

        // Bir sonraki çalışmayı 20 dakika sonra planla
        scheduleNextWork()

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

        // Ana aktiviteyi açmak için bir intent oluşturun
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        // Bildirim oluşturun
        val notification = NotificationCompat.Builder(applicationContext, "channel_id")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.baseline_spellcheck_24)
            .setContentIntent(pendingIntent) // PendingIntent'i bildirimde kullanın
            .setAutoCancel(true) // Bildirime tıklanınca otomatik olarak kapat
            .build()

        // Bildirimi gönderin
        notificationManager.notify(Random().nextInt(), notification)
    }

    private fun scheduleNextWork() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(20, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    companion object {
        private const val WORK_NAME = "NotificationWorker"

        fun startPeriodicWork() {

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
                // Her 25 dakikada bir çalıştır
                20, TimeUnit.MINUTES,
                // Esneklik süresi (isteğe bağlı)
                5, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }// end startPeriodicWork
        }//end companion object
    }

