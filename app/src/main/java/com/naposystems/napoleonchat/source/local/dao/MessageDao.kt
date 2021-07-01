package com.naposystems.napoleonchat.source.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface MessageDao {

    //region Consultas INSERT
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(messageEntity: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessageList(messageEntityList: List<MessageEntity>)
    //endregion

    //region Consultas SELECT
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
                "ORDER BY ${DBConstants.Message.COLUMN_CREATED_AT} ASC"
    )
    fun getMessagesAndAttachments(contact: Int): Flow<List<MessageAttachmentRelation>>

    fun getMessagesAndAttachmentsDistinctUntilChanged(contactId: Int) =
        getMessagesAndAttachments(contactId).distinctUntilChanged()

    //TODO:REVISAR CONSULTA
    @Query(
        "SELECT * FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE (${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} > strftime('%s','now') OR ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} >= 0) " +
                "AND ${DBConstants.Message.COLUMN_ID} IN (SELECT MAX(${DBConstants.Message.COLUMN_ID}) " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "GROUP BY ${DBConstants.Message.COLUMN_CONTACT_ID}) " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC"

    )
    fun getMessagesForHome(): Flow<List<MessageAttachmentRelation>>

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_ID} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :quoteWebId"
    )
    fun getMessageIdByQuoteWebId(quoteWebId: String): Int

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contact " +
                "AND ${DBConstants.Message.COLUMN_STATUS}  = :status " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC"
    )
    fun getMessagesByStatus(contact: Int, status: Int): List<MessageAttachmentRelation>


    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "ORDER BY ${DBConstants.Message.COLUMN_CREATED_AT} DESC " +
                "LIMIT 1"
    )
    suspend fun getLastMessageByContactId(contactId: Int): MessageAttachmentRelation

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_BODY} " +
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
                "AND ${DBConstants.Message.COLUMN_IS_SELECTED} = 1 " +
                "ORDER BY ${DBConstants.Message.COLUMN_ID} DESC"
    )
    fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>>

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
        "SELECT ${DBConstants.Message.COLUMN_CONTACT_ID} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :webId " +
                "LIMIT 1"
    )
    suspend fun getContactIdByWebId(webId: String): Int

    @Query(
        "SELECT ${DBConstants.Message.COLUMN_SELF_DESTRUCTION_AT} " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID} = :webId"
    )
    fun getMessageSelfDestructTimeById(webId: String): Int

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId"
    )
    suspend fun getMessagesByContact(contactId: Int): List<MessageAttachmentRelation>

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId and ${DBConstants.Message.COLUMN_IS_MINE}=0"
    )
    suspend fun getMessagesByContactNotMine(contactId: Int): List<MessageAttachmentRelation>

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

    @Query("SELECT * FROM message WHERE id=:id")
    suspend fun getMessageById(id: Int): MessageAttachmentRelation?

    //TODO:REVISAR CONSULTA
    @Query(
        " SELECT COUNT(CASE WHEN status=3 then 1 end)" +
                " FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                " WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} =:id " +
                " AND ${DBConstants.Message.COLUMN_NUMBER_ATTACHMENTS} <= 1  and ${DBConstants.Message.COLUMN_IS_MINE}=0"
    )
    suspend fun countUnreadByContactId(id: Int): Int

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_MESSAGE_ID} = :messageId"
    )
    fun getMessageByIdAsFlow(messageId: Int): Flow<List<AttachmentEntity>>
    //endregion

    //region Consultas Update
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

    //TODO: Pasar el estado de fallido a una constante
    @Query(
        "UPDATE ${DBConstants.Message.TABLE_NAME_MESSAGE} SET ${DBConstants.Message.COLUMN_UUID} = hex(randomblob(16))" +
                " WHERE ${DBConstants.Message.COLUMN_UUID} IS NULL"
    )
    suspend fun addUUIDMessage()

    //TODO: Revisar el estado fallido de un attachment
    @Query(
        "UPDATE ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} SET ${DBConstants.Attachment.COLUMN_UUID} = hex(randomblob(16))" +
                " WHERE ${DBConstants.Attachment.COLUMN_UUID} IS NULL"
    )
    suspend fun addUUIDAttachment()
    //endregion

    //region Consultas DELETE
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
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId"
    )
    suspend fun deleteMessagesByContactId(contactId: Int)

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_WEB_ID}  = :webId"
    )
    suspend fun deleteMessagesByWebId(webId: String)

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} <> 0 " +
                "AND ${DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT} < strftime('%s','now')"
    )
    fun deleteMessagesByTotalSelfDestructionAt()

    @Query(
        "DELETE " +
                "FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                "WHERE ${DBConstants.Message.COLUMN_CONTACT_ID} = :contactId " +
                "AND ${DBConstants.Message.COLUMN_TYPE_MESSAGE} = :type"
    )
    suspend fun deleteMessageByContactIdAndType(contactId: Int, type: Int)

    //TODO: Pasar el estado de fallido a una constante
    @Query(
        " DELETE FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                " WHERE  ${DBConstants.Message.COLUMN_STATUS} != 5  AND ${DBConstants.Message.COLUMN_ID} NOT IN ( " +
                " SELECT MIN(${DBConstants.Message.COLUMN_ID}) ${DBConstants.Message.COLUMN_ID} " +
                " FROM ${DBConstants.Message.TABLE_NAME_MESSAGE} " +
                " GROUP BY ${DBConstants.Message.COLUMN_WEB_ID}) "
    )
    suspend fun deleteDuplicateMessage()

    //TODO: Verificar el estado de los mensajes
    @Query(
        " DELETE FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                " WHERE  ${DBConstants.Attachment.COLUMN_STATUS} != 3  AND ${DBConstants.Attachment.COLUMN_ID} NOT IN ( " +
                " SELECT MIN(${DBConstants.Attachment.COLUMN_ID}) ${DBConstants.Attachment.COLUMN_ID} " +
                " FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                " GROUP BY ${DBConstants.Attachment.COLUMN_WEB_ID}) "
    )
    suspend fun deleteDuplicateAttachment()
    //endregion
}