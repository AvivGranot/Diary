package com.proactivediary.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.proactivediary.MainActivity
import com.proactivediary.R

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val type = message.data["type"] ?: return
        val destination = message.data["destination"] ?: "note_inbox"

        when (type) {
            "note" -> showNoteNotification(message, destination)
            "quote_winner" -> showQuoteWinnerNotification(message)
            else -> Log.d(TAG, "Unknown FCM type: $type")
        }
    }

    private fun showNoteNotification(message: RemoteMessage, destination: String) {
        val title = message.notification?.title ?: "\uD83D\uDC8C Someone thinks you\u2019re amazing"
        val body = message.notification?.body ?: "Tap to open your anonymous note"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", destination)
            putExtra("noteId", message.data["noteId"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.CHANNEL_NOTES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_NOTE, notification)
    }

    private fun showQuoteWinnerNotification(message: RemoteMessage) {
        val title = message.notification?.title ?: "\uD83C\uDFC6 You\u2019re #1 this week!"
        val body = message.notification?.body ?: "Your quote topped the leaderboard. See who\u2019s catching up."

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("destination", "quotes")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.CHANNEL_NOTES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_QUOTE, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed")
        // Token will be synced to Firestore on next app open via UserProfileRepository
    }

    companion object {
        private const val TAG = "FCMService"
        private const val NOTIFICATION_ID_NOTE = 9001
        private const val NOTIFICATION_ID_QUOTE = 9002
    }
}
