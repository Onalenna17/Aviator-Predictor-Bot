package com.aviatorpredictorbot.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aviatorpredictorbot.MainActivity
import com.aviatorpredictorbot.R
import java.util.Locale

/**
 * Local Notification Manager for Aviator high-probability prediction events.
 * Alerts users with high-priority notifications when strong clearance signals (>=75% safe strength or pink zone) are detected.
 */
class AviatorNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "aviator_high_prob_signals"
        const val CHANNEL_NAME = "High-Probability Predictions"
        const val CHANNEL_DESC = "Alerts when the Aviator Predictor Bot identifies high-probability flight signals"
        private const val NOTIFICATION_ID_BASE = 8900
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 70, 80, 100)
                setShowBadge(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Dispatches a local system notification for a newly identified high-probability round.
     */
    fun sendHighProbabilityAlert(
        roundId: String,
        predictedMultiplier: Double,
        safePercentage: Int,
        autoStopMultiplier: Double,
        targetZone: String,
        enabled: Boolean = true
    ) {
        if (!enabled || !hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            roundId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val formattedMult = String.format(Locale.US, "%.2fx", predictedMultiplier)
        val formattedAutoStop = String.format(Locale.US, "%.2fx", autoStopMultiplier)

        val title = "✈️ High-Probability Signal: $formattedMult ($safePercentage% Safe)"
        val summary = "Round $roundId · Auto-stop @ $formattedAutoStop · $targetZone"
        val detail = "Spribe mathematical model indicates high probability clearance of $safePercentage% for Round $roundId. Target multiplier is $formattedMult with recommended auto-stop at $formattedAutoStop."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_aviator_logo)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + (roundId.filter { it.isDigit() }.toIntOrNull() ?: 1) % 100, notification)
        } catch (_: SecurityException) {
            // Permission not granted at runtime
        }
    }

    /**
     * Sends an immediate test notification to verify the local notification alert pipeline.
     */
    fun sendTestNotification(enabled: Boolean = true) {
        if (!enabled || !hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_aviator_logo)
            .setContentTitle("✈️ Aviator Prediction Alerts Active")
            .setContentText("Test alert: You will be notified whenever high-probability flight signals (≥75% safe) occur.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Test alert: You will be notified whenever high-probability flight signals (≥75% safe strength or 10x+ pink zone) are identified by the Betway Aviator Bot."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
