package com.naposystems.napoleonchat.db.dao.message

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE web_id=:webId")
    suspend fun getMessageByWebId(webId: String): MessageAndAttachment?

    @Query("SELECT * FROM message WHERE id=:id")
    suspend fun getMessageById(id: Int): MessageAndAttachment?

    @Query("SELECT * FROM message WHERE contact_id=:contact AND (total_self_destruction_at > strftime('%s','now') OR total_self_destruction_at >= 0) ORDER BY id ASC")
    fun getMessagesAndAttachments(contact: Int): Flow<List<MessageAndAttachment>>

    fun getMessagesAndAttachmentsDistinctUntilChanged(contactId: Int) =
        getMessagesAndAttachments(contactId).distinctUntilChanged()

    @Query("SELECT *, COUNT(CASE WHEN status=3 AND is_mine=0 THEN 1 END) AS messagesUnReads FROM message WHERE (total_self_destruction_at > strftime('%s','now') OR total_self_destruction_at >= 0) GROUP BY contact_id ORDER BY id DESC")
    fun getMessagesForHome(): Flow<List<MessageAndAttachment>>

    @Query("SELECT id FROM message WHERE web_id=:quoteWebId")
    fun getQuoteId(quoteWebId: String): Int

    @Query("SELECT * FROM message WHERE contact_id=:contact AND status =:status ORDER BY id DESC")
    fun getLocalMessagesByStatus(contact: Int, status: Int): List<MessageAndAttachment>

    @Query("UPDATE message SET is_selected =:isSelected WHERE contact_id=:contact AND id =:idMessage")
    suspend fun updateMessagesSelected(contact: Int, idMessage: Int, isSelected: Int)

    @Query("UPDATE message SET is_selected = 0 WHERE contact_id=:contact")
    suspend fun cleanSelectionMessages(contact: Int)

    @Query("DELETE FROM message WHERE contact_id = :contactId AND id =:messageId")
    suspend fun deleteMessagesSelected(contactId: Int, messageId: Int)

    @Query("DELETE FROM message WHERE contact_id = :contactId AND status =:status AND is_mine = 1")
    suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)

    @Query("SELECT body FROM message WHERE contact_id=:contactId AND is_selected = 1 ORDER BY id ASC")
    suspend fun copyMessagesSelected(contactId: Int): List<String>

    @Query("SELECT * FROM message WHERE contact_id=:contactId ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessageByContact(contactId: Int): MessageAndAttachment

    @Query("SELECT contact_id FROM message WHERE web_id=:messageWebId LIMIT 1")
    suspend fun getContactByMessage(messageWebId: String): Int

    @Query("SELECT * FROM message WHERE contact_id=:contactId AND is_selected = 1 ORDER BY id DESC")
    fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>>

    @Insert
    fun insertMessage(message: Message): Long

    @Insert
    fun insertMessageList(messageList: List<Message>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMessage(message: Message)

    @Query("UPDATE message SET status=:status, updated_At=:updateMessageStatus, total_self_destruction_at=:totalSelfDestructTime WHERE web_id=:webId")
    fun updateMessageStatus(
        webId: String,
        updateMessageStatus: Long,
        totalSelfDestructTime: Long,
        status: Int
    )

    @Query("SELECT id FROM message WHERE web_id =:id")
    fun existMessage(id: String): Int?

    @Query("SELECT DISTINCT * FROM message WHERE contact_id=:contactId AND status=:status AND is_mine=0")
    suspend fun getTextMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment>

    @Query("SELECT DISTINCT * FROM message WHERE contact_id=:contactId AND status=:status AND is_mine=0 AND (type_message=2 OR type_message=3)")
    suspend fun getMissedCallsByStatus(contactId: Int, status: Int): List<MessageAndAttachment>

    @Query("DELETE FROM message WHERE contact_id = :contactId")
    suspend fun deleteMessages(contactId: Int)

    @Query("DELETE FROM message WHERE web_id =:webId")
    suspend fun deletedMessages(webId: String)

    @Query("SELECT contact_id FROM message WHERE web_id =:webId ")
    fun getIdContactWithWebId(webId: String): Int

    @Query("UPDATE message SET self_destruction_at=:selfDestructTime WHERE contact_id=:contactId AND self_destruction_at = 0 AND is_mine = 1")
    suspend fun setSelfDestructTimeByMessages(selfDestructTime: Int, contactId: Int)

    @Query("UPDATE message SET self_destruction_at=:selfDestructTime, total_self_destruction_at = 0, status=:status WHERE web_id =:webId")
    suspend fun updateSelfDestructTimeByMessages(selfDestructTime: Int, webId: String, status: Int)

    @Query("SELECT self_destruction_at FROM message WHERE web_id =:webId")
    fun getSelfDestructTimeByMessage(webId: String): Int

    @Query("DELETE FROM message WHERE total_self_destruction_at <> 0 AND total_self_destruction_at < strftime('%s','now')")
    fun verifyMessagesToDelete()

    @Query("SELECT * FROM message WHERE contact_id=:contactId")
    suspend fun getMessagesByContact(contactId: Int): List<MessageAndAttachment>

    @Query("SELECT * FROM message WHERE contact_id = :contactId AND status =:status AND is_mine = 1")
    suspend fun getMessagesByStatusForMe(contactId: Int, status: Int): List<MessageAndAttachment>

    @Query("DELETE FROM message WHERE contact_id=:contactId AND type_message=:type")
    suspend fun deleteMessageByType(contactId: Int, type: Int)
}