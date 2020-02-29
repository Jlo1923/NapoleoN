package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naposystems.pepito.entity.conversation.Conversation
import com.naposystems.pepito.entity.conversation.ConversationAndContact

@Dao
interface ConversationDao {

    @Insert
    suspend fun insertConversation(conversation: Conversation): Long

    @Query("SELECT * FROM conversation WHERE contact_id=:contactId")
    suspend fun getConversationByContactId(contactId: Int): List<Conversation>

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("UPDATE conversation SET message = '', created = 0, unreads = 0 WHERE contact_id = :idContact")
    suspend fun cleanConversation(idContact: Int)

    @Transaction
    @Query("SELECT * FROM contact AS C INNER JOIN conversation AS CO on C.id=CO.contact_id AND C.status_blocked=0 ORDER BY created DESC")
    fun getConversations(): LiveData<List<ConversationAndContact>>

    @Query("DELETE FROM conversation WHERE contact_id=:contactId")
    fun deleteConversation(contactId: Int)
}