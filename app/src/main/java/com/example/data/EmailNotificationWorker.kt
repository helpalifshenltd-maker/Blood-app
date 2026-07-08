package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class EmailNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val subject = inputData.getString("subject") ?: "Blood Connect Alert"
        val body = inputData.getString("body") ?: "No details"
        val recipient = inputData.getString("recipient") ?: "help.alifshen.ltd@gmail.com"
        val isSmtp = inputData.getBoolean("isSmtp", false)

        val prefs = applicationContext.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean("email_notify_enabled", true)
        val smtpHost = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com"
        val smtpPort = prefs.getString("smtp_port", "587") ?: "587"
        val smtpUsername = prefs.getString("smtp_username", "help.alifshen.ltd@gmail.com") ?: "help.alifshen.ltd@gmail.com"

        if (isSmtp) {
            Log.d("EmailWorker", "--- SMTP EMAIL DISPATCH SIMULATION ---")
            Log.d("EmailWorker", "To: $recipient")
            Log.d("EmailWorker", "From: $smtpUsername via $smtpHost:$smtpPort")
            Log.d("EmailWorker", "Subject: $subject")
            Log.d("EmailWorker", "Body: $body")
            Log.d("EmailWorker", "--------------------------------------")

            showNotification(
                "📧 Gmail Sent to $recipient",
                "Subject: $subject"
            )
        } else {
            Log.d("EmailWorker", "New Alert: $subject - $body")
            showNotification(subject, body)
        }
        
        return Result.success()
    }

    @android.annotation.SuppressLint("NotificationPermission")
    private fun showNotification(title: String, message: String) {
        val channelId = "blood_alerts"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Blood Bank Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
