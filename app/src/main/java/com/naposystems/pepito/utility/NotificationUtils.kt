package com.naposystems.pepito.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.pepito.R
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import java.util.*

object NotificationUtils {

    fun createInformativeNotification(
        context: Context,
        data: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        val sharedPreferencesManager = SharedPreferencesManager(context)
        val title = notification?.title
        val body = notification?.body
        val channelId = context.getString(R.string.default_notification_channel_id)
        val iconBitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.ic_notification_icon
        )

        val builder = NotificationCompat.Builder(
                context,
                channelId
            )
            .setLargeIcon(iconBitmap)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        createNotificationChannel(context, channelId)

        if (data.isNotEmpty() && data.containsKey("type_notification")) {
            when (data.getValue("type_notification").toInt()) {
                4, 5 -> {
                    with(NotificationManagerCompat.from(context)) {
                        notify(Random().nextInt(), builder.build())
                    }
                }

                Constants.NotificationType.ACCOUNT_ATTACK.type -> {

                    val attackerId = data.getValue("attacker_id").toString()

                    sharedPreferencesManager.putInt(
                        Constants.SharedPreferences.PREF_EXISTING_ATTACK,
                        Constants.ExistingAttack.EXISTING.type
                    )
                    sharedPreferencesManager.putString(
                        Constants.SharedPreferences.PREF_ATTCKER_ID, attackerId
                    )

                    with(NotificationManagerCompat.from(context)) {
                        notify(Random().nextInt(), builder.build())
                    }

                    RxBus.publish(RxEvent.AccountAttack())
                }

                Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> RxBus.publish(RxEvent.NewFriendshipRequest())
            }
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.default_notification_channel_id)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}