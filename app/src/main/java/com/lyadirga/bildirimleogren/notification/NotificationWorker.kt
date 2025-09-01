package com.lyadirga.bildirimleogren.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.getLanguageSet
import com.lyadirga.bildirimleogren.ui.MainActivity

class NotificationWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {

        val prefData = PrefData(context)
        var index = prefData.getIndex()
        val currentCalismaSetiIndex = prefData.getCalismaSeti()

        val currentCalismaSeti = if (currentCalismaSetiIndex >= 100) {
            prefData.getLanguageSets()[currentCalismaSetiIndex - 100]
        } else {
            getLanguageSet(currentCalismaSetiIndex)!!
        }

        val setSize = currentCalismaSeti.items.size

        // index güncelle
        index = (index + 1) % setSize
        prefData.setIndex(index)

        // bildirim gönder
        val languageModel = currentCalismaSeti.items[index]
        showNotification(languageModel.wordOrSentence, languageModel.meaning, languageModel.imageResId)

        return Result.success()
    }

    private fun showNotification(title: String, message: String, imageResId: Int?) {
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
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent) // PendingIntent'i bildirimde kullan
            .setAutoCancel(true) // Bildirime tıklanınca otomatik olarak kapat
            .setSmallIcon(R.mipmap.app_icon)

        // imageResId null değilse büyük resim ekle
        imageResId?.let {
            val largeImage = BitmapFactory.decodeResource(applicationContext.resources, it)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(largeImage)
            )
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}