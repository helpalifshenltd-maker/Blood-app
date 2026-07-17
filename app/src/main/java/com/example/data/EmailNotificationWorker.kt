package com.example.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val subject = inputData.getString("subject") ?: ""
        val body = inputData.getString("body") ?: ""
        val recipient = inputData.getString("recipient") ?: ""
        val isSmtp = inputData.getBoolean("isSmtp", false)

        Log.d("EmailNotificationWorker", "Background Email job started.")
        Log.d("EmailNotificationWorker", "Recipient: $recipient")
        Log.d("EmailNotificationWorker", "Subject: $subject")
        Log.d("EmailNotificationWorker", "isSmtp: $isSmtp")

        try {
            // Log for visibility
            Log.d("EmailNotificationWorker", "---- EMAIL CONTENT START ----")
            Log.d("EmailNotificationWorker", "To: $recipient")
            Log.d("EmailNotificationWorker", "Subject: $subject")
            Log.d("EmailNotificationWorker", "Body:\n$body")
            Log.d("EmailNotificationWorker", "---- EMAIL CONTENT END ----")

            // Real email sending via SMTP if configured
            val prefs = applicationContext.getSharedPreferences("blood_connect_prefs", Context.MODE_PRIVATE)
            val emailEnabled = prefs.getBoolean("email_notify_enabled", true)
            
            if (emailEnabled) {
                val host = prefs.getString("smtp_host", "smtp.gmail.com") ?: "smtp.gmail.com"
                val portStr = prefs.getString("smtp_port", "587") ?: "587"
                val port = portStr.toIntOrNull() ?: 587
                val username = prefs.getString("smtp_username", "help.alifshen.ltd@gmail.com") ?: "help.alifshen.ltd@gmail.com"
                val password = prefs.getString("smtp_password", "") ?: ""

                if (host.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                    val props = java.util.Properties().apply {
                        put("mail.smtp.auth", "true")
                        if (port == 465) {
                            put("mail.smtp.socketFactory.port", "465")
                            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                            put("mail.smtp.ssl.enable", "true")
                        } else {
                            put("mail.smtp.starttls.enable", "true")
                        }
                        put("mail.smtp.host", host)
                        put("mail.smtp.port", port.toString())
                    }

                    val session = javax.mail.Session.getInstance(props, object : javax.mail.Authenticator() {
                        override fun getPasswordAuthentication(): javax.mail.PasswordAuthentication {
                            return javax.mail.PasswordAuthentication(username, password)
                        }
                    })

                    val message = javax.mail.internet.MimeMessage(session).apply {
                        setFrom(javax.mail.internet.InternetAddress(username))
                        setRecipients(
                            javax.mail.Message.RecipientType.TO,
                            javax.mail.internet.InternetAddress.parse(recipient.ifBlank { "help.alifshen.ltd@gmail.com" })
                        )
                        setSubject(subject)
                        setText(body)
                    }

                    javax.mail.Transport.send(message)
                    Log.d("EmailNotificationWorker", "Email successfully sent via SMTP to $recipient!")
                } else {
                    Log.w("EmailNotificationWorker", "SMTP configuration is incomplete. Skipping SMTP transport.")
                }
            } else {
                Log.d("EmailNotificationWorker", "Email notifications disabled in configuration.")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("EmailNotificationWorker", "Error executing email notification: ${e.message}", e)
            Result.retry()
        }
    }
}
