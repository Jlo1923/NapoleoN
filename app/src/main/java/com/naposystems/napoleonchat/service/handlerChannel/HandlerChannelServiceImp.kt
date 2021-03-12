package com.naposystems.napoleonchat.service.handlerChannel

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.utility.Constants
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class HandlerChannelServiceImp
@Inject constructor(
    private val context: Context,
    private val repository: HandlerChannel.Repository
) : HandlerChannel.Service {

    override fun initializeChannels() {

        if (repository.getNotificationChannelCreated() == Constants.ChannelCreated.FALSE.state) {
            //region Chat Notification
            createCategoryChannel()
            createMessageChannel(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            //endregion

            //region Others
            createNotificationChannel()
            createCallNotificationChannel()
            createAlertsNotificationChannel()
            createUploadNotificationChannel()
            //endregion

            repository.setNotificationChannelCreated()
        }
    }


    override fun getChannel(channelId: String): NotificationChannel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.getNotificationChannel(channelId)
        } else null
    }

    override fun getChannelId(
        channelType: Int,
        contactId: Int?,
        contactNick: String?
    ): String {
        var channelId = ""
        when (channelType) {
            Constants.ChannelType.DEFAULT.type -> {
                channelId = context.getString(
                    R.string.notification_message_channel_id,
                    repository.getNotificationMessageChannelId()
                )
            }
            Constants.ChannelType.CUSTOM.type -> {
                contactId?.let { userContactId ->
                    Timber.d("*TestChannel: contactId $userContactId")
                    val userChannelId = repository.getCustomNotificationChannelId(contactId)
                    userChannelId?.let { id ->
                        Timber.d("*TestChannel: channelId $id")
                        contactNick?.let { nick ->
                            channelId =
                                context.getString(R.string.notification_custom_channel_id, nick, id)
                        }
                    }
                }
            }
        }
        return channelId
    }

    override fun getChannelType(
        notificationType: Int
    ): String {
        return getChannelType(notificationType, null)
    }

    override fun getChannelType(
        notificationType: Int,
        contactId: Int?
    ): String {
        return when (notificationType) {
            Constants.NotificationType.ENCRYPTED_MESSAGE.type -> {
                val contact = contactId?.let {
                    repository.getContactById(it)
                }
                contact?.let {
                    if (it.stateNotification) {
                        context.getString(
                            R.string.notification_custom_channel_id,
                            it.getNickName(),
                            it.notificationId
                        )
                    } else {
                        context.getString(
                            R.string.notification_message_channel_id,
                            repository.getNotificationMessageChannelId()
                        )
                    }
                } ?: kotlin.run {
                    context.getString(
                        R.string.notification_message_channel_id,
                        repository.getNotificationMessageChannelId()
                    )
                }
            }
            else -> {
                context.getString(R.string.default_notification_channel_id)
            }
        }
    }

    private fun getNotificationMessageChannelId(): Int {
        return repository.getNotificationMessageChannelId()
    }

    private fun setNotificationMessageChannelId(newId: Int) {
        repository.setNotificationMessageChannelId(newId)
    }

    //region Handler Channels
    private fun createCategoryChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // The id of the group.
            val groupId = context.getString(R.string.category_channel_chat)
            // The user-visible name of the group.
            val groupName = context.getString(R.string.category_channel_chat)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    groupId,
                    groupName
                )
            )
        }
    }

    private fun createMessageChannel(uri: Uri?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createMessageChannel")

            val id = getNotificationMessageChannelId().plus(1)
            setNotificationMessageChannelId(id)
            val channelId = context.getString(R.string.notification_message_channel_id, id)
            val name = "Notification Message"
            val descriptionText = "Notification Message Description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText

                val audioAttribute = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                setSound(uri, audioAttribute)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.PRIORITY_MAX
                group = context.getString(R.string.category_channel_chat)
            }

//            Timber.d("*TestSong: defaultSoundUri=$defaultSoundUri")

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createNotificationChannel")

            val channelId = context.getString(R.string.default_notification_channel_id)
            val name = context.getString(R.string.default_notification_channel_id)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            channel.setShowBadge(true)
            channel.lockscreenVisibility = NotificationCompat.PRIORITY_MAX
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createCallNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createCallNotificationChannel")

            val (id: String, name) = context.getString(R.string.calls_channel_id) to
                    context.getString(R.string.calls_channel_name)
            val descriptionText = context.getString(R.string.calls_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
                val audioAttribute = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                setSound(null, audioAttribute)
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.PRIORITY_MAX
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createAlertsNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createAlertsNotificationChannel")

            val (id: String, name) = context.getString(R.string.alerts_channel_id) to
                    context.getString(R.string.alerts_channel_name)
            val descriptionText = context.getString(R.string.alerts_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.PRIORITY_LOW
            }

            /*if (Build.VERSION.SDK_INT >= 29) {
                val soundUri = Settings.System.DEFAULT_RINGTONE_URI

                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                channel.setSound(soundUri, audioAttributes)
            }*/

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createUploadNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createUploadNotificationChannel")

            val (id: String, name) = context.getString(R.string.upload_channel_id) to
                    context.getString(R.string.upload_channel_name)
            val descriptionText = context.getString(R.string.upload_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.PRIORITY_LOW
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createCustomChannel(
        uri: Uri?,
        contactId: Int,
        contactNick: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.d("*NotificationTest: createMessageChannel")
            repository.updateStateChannel(contactId, true)
            val id = UUID.randomUUID().toString()
            repository.setCustomNotificationChannelId(contactId, id)
            val channelId =
                context.getString(R.string.notification_custom_channel_id, contactNick, id)
            Timber.d("*TestDelete: created $channelId")
            val name = context.getString(R.string.notification_custom_channel_name, contactNick)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
//                        description = descriptionText

                val audioAttribute = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                setSound(uri, audioAttribute)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.PRIORITY_MAX
                group = context.getString(R.string.category_channel_chat)
            }

//            Timber.d("*TestSong: defaultSoundUri=$defaultSoundUri")

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun getChannelSound(
        channelType: Int,
        contactId: Int?,
        contactNick: String?
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                var notificationChannelId = ""
                when (channelType) {
                    Constants.ChannelType.DEFAULT.type -> {
                        notificationChannelId = context.getString(
                            R.string.notification_message_channel_id,
                            repository.getNotificationMessageChannelId()
                        )
                    }
                    Constants.ChannelType.CUSTOM.type -> {
                        contactId?.let {
                            val channelId = repository.getCustomNotificationChannelId(contactId)
                            Timber.d("*TestChannelSound: ChannelId $channelId")
                            channelId?.let { chId ->
                                contactNick?.let { nick ->
                                    notificationChannelId =
                                        context.getString(
                                            R.string.notification_custom_channel_id,
                                            nick,
                                            chId
                                        )
                                    Timber.d("*TestChannelSound: notificationChannelId $notificationChannelId")
                                }
                            }
                        }
                    }
                }

                val channel = notificationManager.getNotificationChannel(notificationChannelId)
                channel.sound
            } catch (e: Exception) {
                null
            }
        } else null
    }


    override fun updateChannel(
        uri: Uri?,
        channelType: Int,
        contactId: Int?,
        contactNick: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationChannelId =
//                NotificationCompat.getChannelId(context, channelType, contactId, contactNick)

//            deleteChannel(context, notificationChannelId, null)
//
//            if (channelType == Constants.ChannelType.DEFAULT.type) {
//                createMessageChannel(this.context, uri)
//            } else {
//                contactId?.let { id ->
//                    contactNick?.let { nick ->
//                        createCustomChannel(this.context, uri, id, nick)
//                    }
//                }
//            }
        }
    }

    override fun deleteChannel(
        channelId: String,
        contactId: Int?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val currentChannel = getChannel(channelId)

            Timber.d("*TestChannel: currentChannel $currentChannel")

            currentChannel?.let {
                notificationManager.deleteNotificationChannel(channelId)
                Timber.d("*TestChannel: Eliminado")
            }

            contactId?.let {
                repository.updateStateChannel(contactId, false)
            }
        }
    }

    private fun deleteUserChannel(
        contactId: Int,
        oldNick: String,
        notificationId: String?
    ) {

        Timber.d("*TestDelete: id $contactId, nick $oldNick")

        val channelId = if (notificationId != null) {
            Timber.d("*TestDelete: exist Channel $notificationId")
            context.getString(R.string.notification_custom_channel_id, oldNick, notificationId)
        } else {
            Timber.d("*TestDelete: no exist Channel")
            getChannelId(
                Constants.ChannelType.CUSTOM.type,
                contactId,
                oldNick
            )
        }

        Timber.d("*TestDelete: ChannelId $channelId")

        deleteChannel(channelId, contactId)
    }

    //endregion

}