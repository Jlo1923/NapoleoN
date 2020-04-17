package com.naposystems.pepito.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.naposystems.pepito.R
import com.naposystems.pepito.app.NapoleonApplication
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.utility.Constants.SharedPreferences.PREF_PENDING_CALL
import org.json.JSONObject
import timber.log.Timber
import java.util.*

object NotificationUtils {

    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
    }

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

        if (data.isNotEmpty() && data.containsKey(Constants.TYPE_NOTIFICATION)) {
            when (data.getValue(Constants.TYPE_NOTIFICATION).toInt()) {

                Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                    setupNotificationSound(context, R.raw.sound_encrypted_message)
                }

                Constants.NotificationType.NEW_FRIENDSHIP_REQUEST.type -> {
                    RxBus.publish(RxEvent.NewFriendshipRequest())
                }

                Constants.NotificationType.FRIEND_REQUEST_ACCEPTED.type -> {
                    RxBus.publish(RxEvent.FriendshipRequestAccepted())
                }

                Constants.NotificationType.VERIFICATION_CODE.type, Constants.NotificationType.SUBSCRIPTION.type -> {
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
                        Constants.SharedPreferences.PREF_ATTACKER_ID, attackerId
                    )

                    with(NotificationManagerCompat.from(context)) {
                        notify(Random().nextInt(), builder.build())
                    }

                    RxBus.publish(RxEvent.AccountAttack())
                }

                Constants.NotificationType.INCOMING_CALL.type -> {
                    if (context is NapoleonApplication) {
                        val app: NapoleonApplication = context
                        Timber.d("IsAppVisible: ${app.isAppVisible()}")
                        if (!app.isAppVisible()) {

                            val jsonObject = JSONObject()
                            jsonObject.put(
                                Constants.CallKeys.CHANNEL,
                                data[Constants.CallKeys.CHANNEL]
                            )
                            jsonObject.put(
                                Constants.CallKeys.IS_VIDEO_CALL,
                                data[Constants.CallKeys.IS_VIDEO_CALL] == "true"
                            )
                            jsonObject.put(
                                Constants.CallKeys.CONTACT_ID,
                                data[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0
                            )

                            sharedPreferencesManager.putString(
                                PREF_PENDING_CALL,
                                jsonObject.toString()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupNotificationSound(context: Context, sound: Int) {
        try {
            mediaPlayer.apply {
                reset()
                setDataSource(
                    context,
                    Uri.parse("android.resource://" + context.packageName + "/" + sound)
                )
                if (isPlaying) {
                    stop()
                    reset()
                    release()
                }
                prepare()
                start()
            }
        } catch (e: Exception) {
            Timber.e(e)
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