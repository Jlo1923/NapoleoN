package com.naposystems.napoleonchat.repository.selfDestructTime

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.db.dao.contact.ContactDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class SelfDestructTimeRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val messageDataSource: MessageDataSource,
    private val contactDataSource: ContactDataSource
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
        messageDataSource.setSelfDestructTimeByMessages(selfDestructTime, contactId)
        contactDataSource.setSelfDestructTimeByContact(selfDestructTime, contactId)
    }

    override suspend fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int> {
        return contactDataSource.getSelfDestructTimeByContact(contactId)
    }

    override fun getMessageSelfDestructTimeNotSent(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
        )
    }
}