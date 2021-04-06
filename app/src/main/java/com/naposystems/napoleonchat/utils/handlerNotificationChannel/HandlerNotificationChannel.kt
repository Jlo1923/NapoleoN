package com.naposystems.napoleonchat.utils.handlerNotificationChannel

import android.app.NotificationChannel
import android.net.Uri

interface HandlerNotificationChannel {

    fun initializeChannels()

    fun getChannel(channelId: String): NotificationChannel?

    fun getChannelId(channelType: Int, contactId: Int?, contactNick: String?): String

    fun getChannelSound(channelType: Int, contactId: Int?, contactNick: String?): Uri?

    fun getChannelType(notificationType: Int): String
    fun getChannelType(notificationType: Int, contactId: Int): String

    fun updateChannel(uri: Uri?, channelType: Int, contactId: Int?, contactNick: String?)

    fun updateNickNameChannel(
        contactId: Int,
        oldNick: String,
        newNick: String
    )

    fun deleteUserChannel(contactId: Int, oldNick: String)

    fun deleteUserChannel(contactId: Int, oldNick: String, notificationId: String?)

    fun deleteChannel(channelId: String, contactId: Int?)

}