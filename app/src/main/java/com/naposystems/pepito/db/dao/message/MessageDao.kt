package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE contact_id=:contact ORDER BY id DESC")
    fun getMessagesAndAttachments(contact: Int): DataSource.Factory<Int, MessageAndAttachment>

    @Query("UPDATE message SET is_selected =:isSelected WHERE contact_id=:contact AND id =:idMessage")
    suspend fun updateMessagesSelected(contact: Int, idMessage : Int, isSelected : Int)

    @Query("UPDATE message SET is_selected = 0 WHERE contact_id=:contact")
    suspend fun cleanSelectionMessages(contact: Int)

    @Query("DELETE FROM message WHERE contact_id = :contactId AND is_selected = 1")
    suspend fun deleteMessagesSelected(contactId: Int)

    @Query("SELECT body FROM message WHERE contact_id=:contactId AND is_selected = 1 ORDER BY id ASC")
    suspend fun copyMessagesSelected(contactId: Int) : List<String>

    @Query("SELECT * FROM message WHERE contact_id=:contactId ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessageByContact(contactId: Int): MessageAndAttachment

    @Query("SELECT * FROM message WHERE contact_id=:contactId AND is_selected = 1 ORDER BY id DESC")
    fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>>

    @Insert
    fun insertMessage(message: Message): Long

    @Insert
    fun insertMessageList(messageList: List<Message>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMessage(message: Message)

    @Query("UPDATE message SET status=:status WHERE web_id=:webId")
    fun updateMessageStatus(webId: String, status: Int)

    @Query("SELECT web_id FROM message WHERE contact_id=:contactId AND status=:status AND is_mine=0")
    suspend fun getMessagesByStatus(contactId: Int, status: Int): List<String>

    @Query("DELETE FROM message WHERE contact_id = :contactId")
    suspend fun deleteMessages(contactId: Int)

    @Query("DELETE FROM message WHERE web_id =:webId")
    suspend fun deletedMessages(webId: String)

    @Query("SELECT contact_id FROM message WHERE web_id =:webId ")
    fun getIdContactWithWebId(webId: String) : Int
}