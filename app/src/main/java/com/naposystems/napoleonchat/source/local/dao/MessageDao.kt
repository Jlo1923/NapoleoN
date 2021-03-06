package com.naposystems.napoleonchat.source.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface MessageDao {

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :webId"
    )
    suspend fun getMessageByWebId(webId: String): MessageAttachmentRelation?

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contact " +
                "AND (${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} > strftime('%s','now') OR ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} >= 0) " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} ASC"
    )
    fun getMessagesAndAttachments(contact: Int): Flow<List<MessageAttachmentRelation>>

    fun getMessagesAndAttachmentsDistinctUntilChanged(contactId: Int) =
        getMessagesAndAttachments(contactId).distinctUntilChanged()

    @Query(
        "SELECT *, COUNT(CASE WHEN status=3 " +
                "AND ${DBConstants.Message.COLUMN_IS_MINE}=0 THEN 1 END) AS messagesUnReads " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE (${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} > strftime('%s','now') OR ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} >= 0) " +
                "GROUP BY ${DBConstants.Message.COLUMN_CONTACT_ID} " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC"
    )
    fun getMessagesForHome(): Flow<List<MessageAttachmentRelation>>

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_ID} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :quoteWebId"
    )
    fun getQuoteId(quoteWebId: String): Int

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contact " +
                "AND ${DBConstants.Message.COLUMN_STATUS}  = :status " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC"
    )
    fun getLocalMessagesByStatus(contact: Int, status: Int): List<MessageAttachmentRelation>

    @Query(
        "UPDATE ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "SET ${DBConstants.Message.COLUMN_IS_SELECTED}  = :isSelected " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contact " +
                "AND ${DBConstants.Message.COLUMN_ID}  = :idMessage"
    )
    suspend fun updateMessagesSelected(contact: Int, idMessage: Int, isSelected: Int)

    @Query(
        "UPDATE ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "SET ${DBConstants.Message.COLUMN_IS_SELECTED} = 0 " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contact"
    )
    suspend fun cleanSelectionMessages(contact: Int)

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_ID}  = :messageId"
    )
    suspend fun deleteMessagesSelected(contactId: Int, messageId: Int)

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_STATUS}  = :status " +
                "AND ${DBConstants.Message.COLUMN_IS_MINE} = 1"
    )
    suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)

    @Query(
        "SELECT body " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_IS_SELECTED} = 1 " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} ASC"
    )
    suspend fun copyMessagesSelected(contactId: Int): List<String>

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC " +
                "LIMIT 1"
    )
    suspend fun getLastMessageByContact(contactId: Int): MessageAttachmentRelation

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_CONTACT_ID} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :messageWebId " +
                "LIMIT 1"
    )
    suspend fun getContactByMessage(messageWebId: String): Int

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_IS_SELECTED} = 1 " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC"
    )
    fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>>

    @Insert
    fun insertMessage(messageEntity: MessageEntity): Long

    @Insert
    fun insertMessageList(messageEntityList: List<MessageEntity>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMessage(messageEntity: MessageEntity)

    @Query(
        "UPDATE ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "SET ${DBConstants.Message.COLUMN_STATUS} = :status, " +
                "${DBConstants.Message.COLUMN_UPDATED_AT} = :updateMessageStatus, " +
                "${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} = :totalSelfDestructTime " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :webId"
    )
    fun updateMessageStatus(
        webId: String,
        updateMessageStatus: Long,
        totalSelfDestructTime: Long,
        status: Int
    )

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_ID} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID}  = :id"
    )
    fun existMessage(id: String): Int?

    @Query(
        "SELECT DISTINCT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_STATUS} = :status " +
                "AND ${DBConstants.Message.COLUMN_IS_MINE} = 0"
    )
    suspend fun getTextMessagesByStatus(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation>

    @Query(
        "SELECT DISTINCT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_STATUS} = :status " +
                "AND ${DBConstants.Message.COLUMN_IS_MINE} = 0 " +
                "AND (${DBConstants.Message.COLUMN_TYPE_MESSAGE} = 2 OR ${DBConstants.Message.COLUMN_TYPE_MESSAGE} = 3)"
    )
    suspend fun getMissedCallsByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId"
    )
    suspend fun deleteMessages(contactId: Int)

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID}  = :webId"
    )
    suspend fun deletedMessages(webId: String)

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_CONTACT_ID} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID}  = :webId "
    )
    fun getIdContactWithWebId(webId: String): Int

    @Query(
        "UPDATE ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "SET ${DBConstants.Message.COLUMN_SELF_DESTRUCTION_AT} = :selfDestructTime " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_SELF_DESTRUCTION_AT} = 0 " +
                "AND ${DBConstants.Message.COLUMN_IS_MINE} = 1"
    )
    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)

    @Query(
        "UPDATE ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "SET ${DBConstants.Message.COLUMN_SELF_DESTRUCTION_AT} = :selfDestructTime, " +
                "${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} = 0, " +
                "${DBConstants.Message.COLUMN_STATUS} = :status " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :webId"
    )
    suspend fun updateSelfDestructTimeByMessages(selfDestructTime: Int, webId: String, status: Int)

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_SELF_DESTRUCTION_AT} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :webId"
    )
    fun getSelfDestructTimeByMessage(webId: String): Int

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} <> 0 " +
                "AND ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} < strftime('%s','now')"
    )
    fun verifyMessagesToDelete()

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId"
    )
    suspend fun getMessagesByContact(contactId: Int): List<MessageAttachmentRelation>

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_STATUS}  = :status " +
                "AND ${DBConstants.Message.COLUMN_IS_MINE} = 1"
    )
    suspend fun getMessagesByStatusForMe(
        contactId: Int,
        status: Int
    ): List<MessageAttachmentRelation>

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_TYPE_MESSAGE} = :type"
    )
    suspend fun deleteMessageByType(contactId: Int, type: Int)

    @Query("SELECT * FROM message WHERE id=:id")
    suspend fun getMessageById(id: Int): MessageAttachmentRelation?

}