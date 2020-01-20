package com.naposystems.pepito.entity.conversation

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationAndAttachment(
    @Embedded
    var conversation: Conversation,
    @Relation(
        parentColumn = "id",
        entityColumn = "conversation_id",
        entity = ConversationAttachment::class
    )
    var attachmentList: List<ConversationAttachment>
)