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
            // Perform simulated sending or background logging
            // Since this is a prototype, we log the complete email structure
            Log.d("EmailNotificationWorker", "---- EMAIL CONTENT START ----")
            Log.d("EmailNotificationWorker", "To: $recipient")
            Log.d("EmailNotificationWorker", "Subject: $subject")
            Log.d("EmailNotificationWorker", "Body:\n$body")
            Log.d("EmailNotificationWorker", "---- EMAIL CONTENT END ----")

            // Real email sending could be integrated here, for now we successfully complete the job
            Result.success()
        } catch (e: Exception) {
            Log.e("EmailNotificationWorker", "Error executing email notification: ${e.message}", e)
            Result.retry()
        }
    }
}
