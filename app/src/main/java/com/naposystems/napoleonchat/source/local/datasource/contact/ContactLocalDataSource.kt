package com.naposystems.napoleonchat.source.local.datasource.contact

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

interface ContactLocalDataSource {

    suspend fun getContacts(): LiveData<MutableList<ContactEntity>>

    suspend fun getLocaleContacts(): List<ContactEntity>

    fun getContact(contactId : Int): LiveData<ContactEntity>

    fun getContactById(contactId : Int): ContactEntity?

    suspend fun restoreContact(contactId: Int)

    suspend fun updateNameFakeContact(contactId: Int, nameFake: String)

    suspend fun updateNicknameFakeContact(contactId: Int, nicknameFake: String)

    suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String)

    suspend fun insertContact(contact: ContactEntity)

    suspend fun insertOrUpdateContactList(contactList: List<ContactEntity>, location : Int = 0): List<ContactEntity>

    fun getBlockedContacts() : LiveData<List<ContactEntity>>

    suspend fun blockContact(contactId: Int)

    suspend fun unblockContact(contactId: Int)

    suspend fun deleteContact(contact: ContactEntity)

    suspend fun deleteContacts(contacts: List<ContactEntity>)

    suspend fun updateContactSilenced(contactId: Int, contactSilenced : Int)

    suspend fun getContactSilenced(contactId: Int) : Boolean

    suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

    suspend fun getSelfDestructTimeByContact(contactId: Int) : LiveData<Int>

    suspend fun restoreImageByContact(contactId: Int)

    suspend fun updateChannelId(contactId: Int, channelId: String)

    suspend fun updateStateChannel(contactId: Int, state:Boolean)
}