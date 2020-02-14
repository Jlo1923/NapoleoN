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

    @Transaction
    @Query("SELECT * FROM contact AS C INNER JOIN conversation AS CO on C.id=CO.contact_id")
    fun getConversations(): LiveData<List<ConversationAndContact>>
}