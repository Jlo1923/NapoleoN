package com.naposystems.napoleonchat.service.handlerChannel

import android.app.NotificationChannel
import android.net.Uri
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface HandlerChannel {
    interface Service {

        fun initializeChannels()

        fun getChannel(channelId: String): NotificationChannel?

        fun getChannelId(channelType: Int, contactId: Int?, contactNick: String?): String

        fun getChannelSound(channelType: Int, contactId: Int?, contactNick: String?): Uri?

        fun getChannelType(notificationType: Int): String
        fun getChannelType(notificationType: Int, contactIdNotification: Int?): String

        fun updateChannel(uri: Uri?, channelType: Int, contactId: Int?, contactNick: String?)

//        fun deleteUserChannel(contactId: Int, oldNick: String, notificationId: String?)

        fun deleteChannel(channelId: String, contactId: Int?)

//        fun getNotificationMessageChannelId(): Int

//        fun setNotificationMessageChannelId(newId: Int)

    }

    interface Repository {

        fun getContactById(contactId: Int): ContactEntity?

        fun updateStateChannel(contactId: Int, state: Boolean)

        fun getNotificationMessageChannelId(): Int
        fun setNotificationMessageChannelId(newId: Int)

        fun getNotificationChannelCreated(): Int
        fun setNotificationChannelCreated()

        fun getCustomNotificationChannelId(contactId: Int): String?
        fun setCustomNotificationChannelId(contactId: Int, newId: String)
    }

}