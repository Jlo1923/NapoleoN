package com.naposystems.pepito.entity.message

import androidx.room.Embedded
import androidx.room.Relation
import com.naposystems.pepito.entity.message.attachments.Attachment

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