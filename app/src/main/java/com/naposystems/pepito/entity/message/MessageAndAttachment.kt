package com.naposystems.pepito.entity.message

import androidx.room.Embedded
import androidx.room.Relation

data class MessageAndAttachment(
    @Embedded
    var message: Message,
    @Relation(
        parentColumn = "id",
        entityColumn = "message_id",
        entity = Attachment::class
    )
    var attachmentList: List<Attachment>
)