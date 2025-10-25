package com.example.movietracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.movietracker.R

object NotificationHelper {
  private const val CHANNEL_ID_LIKED = "liked_updates"
  private const val CHANNEL_NAME_LIKED = "Liked updates"
  private const val CHANNEL_DESC_LIKED = "Notifications when you like or unlike items"
  private const val TAG = "NotificationHelper"

  private fun ensureChannels(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
      CHANNEL_ID_LIKED,
      CHANNEL_NAME_LIKED,
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      description = CHANNEL_DESC_LIKED
    }
    manager.createNotificationChannel(channel)
  }

  fun notifyLiked(context: Context, title: String, liked: Boolean) {
    ensureChannels(context)
    val text = if (liked) context.getString(
      R.string.added_to_liked,
      title
    ) else context.getString(R.string.removed_from_liked, title)
    val builder = NotificationCompat.Builder(context, CHANNEL_ID_LIKED)
      .setSmallIcon(R.drawable.ic_liked)
      .setContentTitle(context.getString(R.string.app_name))
      .setContentText(text)
      .setAutoCancel(true)

    val managerCompat = NotificationManagerCompat.from(context)

    val hasPermission =
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
      Log.w(TAG, "Notification permission not granted; skipping notify()")
      return
    }

    if (!managerCompat.areNotificationsEnabled()) {
      Log.w(TAG, "Notifications are disabled for the app; skipping notify()")
      return
    }

    try {
      managerCompat.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
    } catch (se: SecurityException) {
      Log.w(TAG, "Failed to post notification due to missing permission", se)
    }
  }
}
