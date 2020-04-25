package com.naposystems.pepito.utility.notificationUtils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.naposystems.pepito.repository.notificationUtils.NotificationUtilsRepository
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Constants.SharedPreferences.PREF_PENDING_CALL
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.android.support.DaggerApplication
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class NotificationUtils @Inject constructor(applicationContext: Context) {

    @Inject
    lateinit var repository: NotificationUtilsRepository

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

    init {
        (applicationContext as DaggerApplication).androidInjector().inject(this)
    }

    fun createInformativeNotification(
        context: Context,
        data: Map<String, String>,
        notification: RemoteMessage.Notification?
    ) {
        var notificationType = 0
        val sharedPreferencesManager =
            SharedPreferencesManager(context)
        val title = notification?.title
        val body = notification?.body
        val channelId = context.getString(R.string.default_notification_channel_id)
        val iconBitmap = BitmapFactory.decodeResource(
            context.resources, R.drawable.ic_notification_icon
        )

        val pair =
            createPendingIntent(
                context,
                data,
                notificationType
            )
        val pendingIntent = pair.first
        notificationType = pair.second

        val builder = NotificationCompat.Builder(
            context,
            channelId
        )
            .setLargeIcon(iconBitmap)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        createNotificationChannel(
            context,
            channelId
        )

        handleNotificationType(
            notificationType,
            data,
            builder,
            context,
            sharedPreferencesManager
        )
    }

    private fun createPendingIntent(
        context: Context,
        data: Map<String, String>,
        notificationType: Int
    ): Pair<PendingIntent, Int> {
        var notificationType1 = notificationType
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        with(data) {
            if (this.isNotEmpty()) {
                val typeNotificationKey =
                    Constants.NotificationKeys.TYPE_NOTIFICATION
                val contactKey =
                    Constants.NotificationKeys.CONTACT

                if (this.containsKey(typeNotificationKey)) {
                    notificationType1 = this.getValue(typeNotificationKey).toInt()
                    notificationIntent.putExtra(typeNotificationKey, notificationType1.toString())
                }

                if (this.containsKey(contactKey)) {
                    notificationIntent.putExtra(contactKey, this.getValue(contactKey).toString())
                }
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
        )
        return Pair(pendingIntent, notificationType1)
    }

    private fun handleNotificationType(
        notificationType: Int,
        data: Map<String, String>,
        builder: NotificationCompat.Builder,
        context: Context,
        sharedPreferencesManager: SharedPreferencesManager
    ) {
        var app: NapoleonApplication? = null
        if (context is NapoleonApplication) {
            app = context
            Timber.d("IsAppVisible:${app.isAppVisible()}")
        }
        when (notificationType) {

            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                /*{message_id=ec45fe6f-0b7d-4255-9549-35ef9cedf2e6,
                body=Has recibido un mensaje cifrado,
                sound=default,
                title=Mensaje Cifrado,
                contact=194097,
                type_notification=1,
                silence=false}*/
                val titleKey =
                    Constants.NotificationKeys.TITLE
                val bodyKey =
                    Constants.NotificationKeys.BODY
                val messageId =
                    Constants.NotificationKeys.MESSAGE_ID

                if (data.containsKey(titleKey)) {
                    builder.setContentTitle(data.getValue(titleKey))
                }

                if (data.containsKey(bodyKey)) {
                    builder.setContentText(data.getValue(bodyKey))
                }

                if (data.containsKey(messageId) && app != null && !app.isAppVisible()) {
                    repository.notifyMessageReceived(data.getValue(messageId))
                }

                with(NotificationManagerCompat.from(context)) {
                    notify(Random().nextInt(), builder.build())
                }
                Timber.d("Mensaje perro")
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
                if (app != null && !app.isAppVisible()) {
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