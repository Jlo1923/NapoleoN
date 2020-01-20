package com.naposystems.pepito.db.dao.conversationAttachment

import com.naposystems.pepito.entity.conversation.ConversationAttachment
import javax.inject.Inject

class ConversationAttachmentLocalDataSource @Inject constructor(
    private val conversationAttachmentDao: ConversationAttachmentDao
) : ConversationAttachmentDataSource {

    override fun insertConversationAttachment(listAttachment: List<ConversationAttachment>): List<Long> {
        return conversationAttachmentDao.insertConversationAttachment(listAttachment)
    }

    override fun updateConversationAttachments(listAttachment: List<ConversationAttachment>) {
        conversationAttachmentDao.updateConversationAttachments(listAttachment)
    }
}