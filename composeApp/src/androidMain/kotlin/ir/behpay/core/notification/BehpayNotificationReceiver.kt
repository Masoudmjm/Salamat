package ir.behpay.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class BehpayNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val title = intent.getStringExtra("title") ?: "یادآور سلامت"
        val body = intent.getStringExtra("body") ?: ""
        val channelId = intent.getStringExtra("channelId") ?: NotificationChannelType.HEALTH_REMINDERS.id
        val payloadRoute = intent.getStringExtra("payloadRoute")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure notification channel exists even if fired in background after reboot
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channel = NotificationChannel(
                NotificationChannelType.HEALTH_REMINDERS.id,
                NotificationChannelType.HEALTH_REMINDERS.nameFa,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = NotificationChannelType.HEALTH_REMINDERS.descriptionFa
                enableLights(true)
                enableVibration(true)
                setSound(defaultSoundUri, null)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to launch app when notification is tapped
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", payloadRoute)
        }

        val contentPendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                id.hashCode(),
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val monochromeId = context.resources.getIdentifier("ic_launcher_monochrome", "drawable", context.packageName)
        val iconRes = if (monochromeId != 0) {
            monochromeId
        } else if (context.applicationInfo.icon != 0) {
            context.applicationInfo.icon
        } else {
            android.R.drawable.ic_dialog_info
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .apply {
                if (contentPendingIntent != null) {
                    setContentIntent(contentPendingIntent)
                }
            }
            .build()

        notificationManager.notify(id.hashCode(), notification)
    }
}
