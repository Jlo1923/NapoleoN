package com.naposystems.napoleonchat.service.handlerNotificationChannel

import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class HandlerChannelRepositoryImp
@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val contactLocalDataSource: ContactLocalDataSource
) : HandlerNotificationChannel.Repository {

    override fun getContactById(contactId: Int): ContactEntity? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun updateStateChannel(contactId: Int, state: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            contactLocalDataSource.updateStateChannel(contactId, state)
        }
    }

    override fun getNotificationMessageChannelId(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID
        )
    }

    override fun setNotificationMessageChannelId(newId: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID,
            newId
        )
    }

    override fun getNotificationChannelCreated(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_CHANNEL_CREATED)
    }

    override fun setNotificationChannelCreated() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_CHANNEL_CREATED,
            Constants.ChannelCreated.TRUE.state
        )
    }

    override fun getCustomNotificationChannelId(contactId: Int): String? {
        val contact = contactLocalDataSource.getContactById(contactId)
        return contact?.notificationId
    }

    override fun setCustomNotificationChannelId(contactId: Int, newId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            contactLocalDataSource.updateChannelId(contactId, newId)
        }
    }

}