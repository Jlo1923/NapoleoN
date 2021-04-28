package com.naposystems.napoleonchat.ui.conversation.model

import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.utility.Constants
import java.util.*

data class ItemMessageWithMsgEntity(
    val messageEntity: MessageEntity,
    val attachment: AttachmentEntity? = null,
    val numberAttachments: Int = 0,
    val selfDestructTime: Int,
    val quote: String,
    var contact: ContactEntity? = null
) {

    fun toMessageReqDto(cryptoMessage: CryptoMessage): MessageReqDTO = MessageReqDTO(
        userDestination = contact?.id ?: 0,
        quoted = quote,
        body = messageEntity.getBody(cryptoMessage),
        numberAttachments = numberAttachments,
        destroy = selfDestructTime,
        messageType = Constants.MessageTextType.NORMAL.type,
        uuidSender = messageEntity.uuid ?: UUID.randomUUID().toString()
    )

}


