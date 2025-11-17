package com.example.lab_week_08

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var handler: Handler

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        notificationBuilder = startForegroundService()
        val thread = HandlerThread("SecondServiceThread").apply { start() }
        handler = Handler(thread.looper)
    }

    private fun startForegroundService(): NotificationCompat.Builder {

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )

        val channelId = "002"

        // ⛔ Error kamu di sini → harus pakai check API 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Second Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second Foreground Service")
            .setContentText("Running second notification service...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(2001, builder.build())
        return builder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        handler.post {
            Thread.sleep(3000) // simulasi countdown
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }
}
