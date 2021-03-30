package com.naposystems.napoleonchat.ui.conversation.model

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity

data class ItemMessage(
    val messageString: String = "",
    val attachment: AttachmentEntity? = null,
    val numberAttachments: Int = 0,
    val selfDestructTime: Int,
    val quote: String,
    var contact: ContactEntity? = null
)

fun ItemMessage.toItemMessageWithMsgEntity(
    messageEntity: MessageEntity
): ItemMessageWithMsgEntity {
    return ItemMessageWithMsgEntity(
        messageEntity = messageEntity,
        attachment = attachment,
        numberAttachments = numberAttachments,
        selfDestructTime = selfDestructTime,
        quote = quote,
        contact = contact
    )
}