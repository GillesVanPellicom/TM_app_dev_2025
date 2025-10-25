package com.example.movietracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.movietracker.R

object NotificationHelper {
  private const val CHANNEL_ID_LIKED = "liked_updates"
  private const val CHANNEL_NAME_LIKED = "Liked updates"
  private const val CHANNEL_DESC_LIKED = "Notifications when you like or unlike items"

  private fun ensureChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
  }

  fun notifyLiked(context: Context, title: String, liked: Boolean) {
    ensureChannels(context)
    val text = if (liked) context.getString(R.string.added_to_liked, title) else context.getString(R.string.removed_from_liked, title)
    val builder = NotificationCompat.Builder(context, CHANNEL_ID_LIKED)
      .setSmallIcon(R.drawable.ic_liked)
      .setContentTitle(context.getString(R.string.app_name))
      .setContentText(text)
      .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
      notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
    }
  }
}