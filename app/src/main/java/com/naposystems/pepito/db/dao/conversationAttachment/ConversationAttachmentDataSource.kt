package com.naposystems.pepito.db.dao.conversationAttachment

import com.naposystems.pepito.entity.conversation.ConversationAttachment

interface ConversationAttachmentDataSource {

    fun insertConversationAttachment(listAttachment: List<ConversationAttachment>): List<Long>

    fun updateConversationAttachments(listAttachment: List<ConversationAttachment>)
}