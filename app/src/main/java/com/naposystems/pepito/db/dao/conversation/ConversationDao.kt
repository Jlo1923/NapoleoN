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

    @Query("SELECT unreads FROM conversation WHERE contact_id=:contactId")
    suspend fun getQuantityUnreads(contactId: Int): Int

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("DELETE FROM conversation WHERE contact_id = :contactId")
    suspend fun cleanConversation(contactId: Int)

    @Query("UPDATE conversation SET message =:message, created =:created, status =:status, unreads =:unreads WHERE contact_id = :contactId")
    suspend fun updateConversationByContact(contactId: Int, message: String, created: Int, status: Int, unreads: Int)

    @Transaction
    @Query("SELECT * FROM contact AS C INNER JOIN conversation AS CO on C.id=CO.contact_id AND C.status_blocked=0 ORDER BY created DESC")
    fun getConversations(): LiveData<List<ConversationAndContact>>

    @Query("DELETE FROM conversation WHERE contact_id=:contactId")
    fun deleteConversation(contactId: Int)
}