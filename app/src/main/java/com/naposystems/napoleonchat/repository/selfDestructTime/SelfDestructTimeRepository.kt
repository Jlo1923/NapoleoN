package com.naposystems.napoleonchat.repository.selfDestructTime

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class SelfDestructTimeRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource
) : IContractSelfDestructTime.Repository {

    override fun getSelfDestructTime(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME)
    }

    override fun setSelfDestructTime(selfDestructTime: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_SELF_DESTRUCT_TIME,
            selfDestructTime
        )
    }

    override suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int) {
        messageLocalDataSource.setSelfDestructTimeByMessages(selfDestructTime, contactId)
        contactLocalDataSource.setSelfDestructTimeByContact(selfDestructTime, contactId)
    }

    override suspend fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int> {
        return contactLocalDataSource.getSelfDestructTimeByContact(contactId)
    }

    override suspend fun getSelfDestructTimeAsIntByContact(contactId: Int) : Int {
        return contactLocalDataSource.getSelfDestructTimeAsIntByContact(contactId)
    }

    override fun getMessageSelfDestructTimeNotSent(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
        )
    }
}