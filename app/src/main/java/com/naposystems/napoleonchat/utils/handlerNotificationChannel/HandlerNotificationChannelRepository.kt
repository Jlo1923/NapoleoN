package com.naposystems.napoleonchat.utils.handlerNotificationChannel

import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface HandlerNotificationChannelRepository {

    fun getContactById(contactId: Int): ContactEntity?

    fun updateStateChannel(contactId: Int, state: Boolean)

    fun getNotificationMessageChannelId(): Int
    fun setNotificationMessageChannelId(newId: Int)

    fun getNotificationChannelCreated(): Int
    fun setNotificationChannelCreated()

    fun getCustomNotificationChannelId(contactId: Int): String?
    fun setCustomNotificationChannelId(contactId: Int, newId: String)

}