package com.naposystems.pepito.db.dao.conversationAttachment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.pepito.entity.conversation.ConversationAttachment

@Dao
interface ConversationAttachmentDao {

    @Insert
    fun insertConversationAttachment(listConversationAttachment: List<ConversationAttachment>): List<Long>

    @Update
    fun updateConversationAttachments(listConversationAttachment: List<ConversationAttachment>)
}