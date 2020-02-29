package com.naposystems.pepito.db.dao.message

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE user_addressee=:contact ORDER BY id DESC")
    fun getMessagesAndAttachments(contact: Int): DataSource.Factory<Int, MessageAndAttachment>

    @Query("UPDATE message SET is_selected =:isSelected WHERE user_addressee=:contact AND id =:idMessage")
    suspend fun updateMessagesSelected(contact: Int, idMessage : Int, isSelected : Int)

    @Query("UPDATE message SET is_selected = 0 WHERE user_addressee=:contact")
    suspend fun cleanSelectionMessages(contact: Int)

    @Query("DELETE FROM message WHERE user_addressee = :idContact AND is_selected = 1")
    suspend fun deleteMessagesSelected(idContact: Int)

    @Query("SELECT body FROM message WHERE user_addressee=:idContact AND is_selected = 1 ORDER BY id ASC")
    suspend fun copyMessagesSelected(idContact: Int) : List<String>

    @Query("SELECT * FROM message WHERE user_addressee=:idContact ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessageByContact(idContact: Int): MessageAndAttachment

    @Query("SELECT * FROM message WHERE user_addressee=:idContact AND is_selected = 1 ORDER BY id DESC")
    fun getMessagesSelected(idContact: Int): LiveData<List<MessageAndAttachment>>

    @Insert
    fun insertMessage(message: Message): Long

    @Insert
    fun insertMessageList(messageList: List<Message>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMessage(message: Message)

    @Query("UPDATE message SET status=:status WHERE web_id=:webId")
    fun updateMessageStatus(webId: String, status: Int)

    @Query("SELECT web_id FROM message WHERE status=:status AND is_mine=0")
    suspend fun getMessagesByStatus(status: Int): List<String>

    @Query("DELETE FROM message WHERE user_addressee = :idContact")
    suspend fun deleteMessages(idContact: Int)

    @Query("DELETE FROM message WHERE web_id =:webId")
    suspend fun deletedMessages(webId: String)

    @Query("SELECT user_addressee FROM message WHERE web_id =:webId ")
    fun getIdContactWithWebId(webId: String) : Int
}