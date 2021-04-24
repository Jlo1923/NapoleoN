package com.naposystems.napoleonchat.source.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

@Dao
interface ContactDao {

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_STATUS_BLOCKED} = 0 " +
                "ORDER BY ${DBConstants.Contact.COLUMN_NICKNAME} ASC"
    )
    fun getContacts(): LiveData<MutableList<ContactEntity>>

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_STATUS_BLOCKED} = 0 " +
                "ORDER BY ${DBConstants.Contact.COLUMN_DISPLAY_NAME} ASC"
    )
    suspend fun getLocalContacts(): List<ContactEntity>

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    fun getContactById(contactId: Int): ContactEntity?

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    fun getContact(contactId: Int): LiveData<ContactEntity>

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_SILENCED} = :contactSilenced " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    fun updateContactSilenced(contactId: Int, contactSilenced: Int)

    @Query(
        "SELECT ${DBConstants.Contact.COLUMN_SILENCED} " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    suspend fun getContactSilenced(contactId: Int): Boolean

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET  ${DBConstants.Contact.COLUMN_DISPLAY_NAME_FAKE}  = :nameFake " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    suspend fun updateNameFakeContact(contactId: Int, nameFake: String)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_IMAGE_URL_FAKE} = :avatarFake " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    suspend fun updateAvatarFakeContact(contactId: Int, avatarFake: String)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_NICKNAME_FAKE} = :nickNameFake " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    suspend fun updateNickNameFakeContact(contactId: Int, nickNameFake: String)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_DISPLAY_NAME_FAKE} = '', " +
                "${DBConstants.Contact.COLUMN_NICKNAME_FAKE} = '', " +
                "${DBConstants.Contact.COLUMN_IMAGE_URL_FAKE} = '', " +
                "${DBConstants.Contact.COLUMN_STATE_NOTIFICATION} = 0  " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    suspend fun restoreContact(contactId: Int)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_IMAGE_URL} = '', " +
                "${DBConstants.Contact.COLUMN_IMAGE_URL_FAKE} = '' " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    suspend fun restoreImageByContact(contactId: Int)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_NOTIFICATION_ID} = :channelId " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    suspend fun updateChannelId(contactId: Int, channelId: String)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_STATE_NOTIFICATION} = :state " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    suspend fun updateStateChannel(contactId: Int, state: Boolean)

    @Insert
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_STATUS_BLOCKED} = 1 " +
                "ORDER BY ${DBConstants.Contact.COLUMN_DISPLAY_NAME} ASC"
    )
    fun getBlockedContacts(): LiveData<List<ContactEntity>>

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_STATUS_BLOCKED} = 1 " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    suspend fun blockContact(contactId: Int)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_STATUS_BLOCKED} = 0 " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId"
    )
    suspend fun unblockContact(contactId: Int)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContacts(contacts: List<ContactEntity>)

    @Query(
        "UPDATE ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "SET ${DBConstants.Contact.COLUMN_SELF_DESTRUCT_TIME} = :selfDestructTime " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    suspend fun setSelfDestructTimeByContact(selfDestructTime: Int, contactId: Int)

    @Query(
        "SELECT ${DBConstants.Contact.COLUMN_SELF_DESTRUCT_TIME} " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    fun getSelfDestructTimeByContact(contactId: Int): LiveData<Int>

    @Query(
        "SELECT ${DBConstants.Contact.COLUMN_SELF_DESTRUCT_TIME} " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    fun getSelfDestructTimeAsIntByContact(contactId: Int): Int

    @Query(
        "SELECT ${DBConstants.Contact.COLUMN_SELF_DESTRUCT_TIME} " +
                "FROM ${DBConstants.Contact.TABLE_NAME_CONTACT} " +
                "WHERE ${DBConstants.Contact.COLUMN_ID} = :contactId "
    )
    fun getSelfDestructTimeByContactWithOutLiveData(contactId: Int): Int


}