package com.naposystems.pepito.db.dao.message

import androidx.paging.DataSource
import androidx.room.*
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE user_addressee=:contact ORDER BY id DESC")
    fun getMessagesAndAttachments(contact: Int): DataSource.Factory<Int, MessageAndAttachment>

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
}