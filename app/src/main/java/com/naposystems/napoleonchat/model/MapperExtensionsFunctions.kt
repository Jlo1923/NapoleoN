package com.naposystems.napoleonchat.model

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils

//region Funciones de Extension de Post
fun Map<String, String>.toCallModel(): CallModel {

    var channel = ""
    var contactId = 0
    var isVideoCall = false
    var offer = ""

    if (containsKey(Constants.CallKeys.CHANNEL_NAME))
        channel = "presence-${this[Constants.CallKeys.CHANNEL_NAME]}"

    if (containsKey(Constants.CallKeys.IS_VIDEO_CALL))
        isVideoCall = this[Constants.CallKeys.IS_VIDEO_CALL] == "true"

    if (containsKey(Constants.CallKeys.CONTACT_ID))
        contactId = this[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0

    if (containsKey(Constants.CallKeys.OFFER))
        offer = Utils.decoderOffer(this[Constants.CallKeys.OFFER].toString())

    return CallModel(
        contactId,
        channel,
        isVideoCall,
        offer
    )
}

fun List<NewMessageEventMessageRes>.toMessagesReqDTO(mustStatus: Constants.StatusMustBe): MessagesReqDTO {

    val messages = filter {
        it.attachments.isEmpty()
    }.map {
        MessageDTO(
            id = it.id,
            type = Constants.MessageType.TEXT.type,
            user = it.userAddressee,
            status = mustStatus.status
        )
    }.toMutableList()

    val attachments = filter {
        it.attachments.isNotEmpty()
    }.flatMap {
        it.attachments
    }.map {
        mappingMessagesDto(
            attachment = it,
            list = this,
            mustStatus
        )
    }

    messages.addAll(attachments)

    return MessagesReqDTO(messages)

}

fun List<MessageResDTO>.toMessagesReqDTOFrom(mustStatus: Constants.StatusMustBe): MessagesReqDTO {

    val messages = filter {
        it.attachments.isEmpty()
    }.map {
        MessageDTO(
            id = it.id,
            type = Constants.MessageType.TEXT.type,
            user = it.userAddressee,
            status = mustStatus.status
        )
    }.toMutableList()

    val attachments = filter {
        it.attachments.isNotEmpty()
    }.flatMap {
        it.attachments
    }.map {
        mappingMessagesDtoFrom(
            attachment = it,
            list = this,
            mustStatus
        )
    }

    messages.addAll(attachments)

    return MessagesReqDTO(
        messages
    )

}

fun List<MessageAttachmentRelation>.toMessagesReqDTOFromRelation(
    statusReceived: Constants.StatusMustBe
): MessagesReqDTO {

    val messages = filter {
        it.attachmentEntityList.isEmpty()
    }.map {
        MessageDTO(
            id = it.messageEntity.webId,
            type = Constants.MessageType.TEXT.type,
            user = it.messageEntity.contactId,
            status = statusReceived.status
        )
    }.toMutableList()

    /**
     * Se envian ademas los tipos attachments
     */
    val attachments = filter {
        it.attachmentEntityList.isNotEmpty()
    }.flatMap { messageAndAttachmentRelation ->

        val contactId = messageAndAttachmentRelation.messageEntity.contactId

        messageAndAttachmentRelation.attachmentEntityList.map { attachmentEntity ->
            MessageDTO(
                id = attachmentEntity.webId,
                type = Constants.MessageType.ATTACHMENT.type,
                user = contactId,
                status = statusReceived.status
            )
        }

    }

    messages.addAll(attachments)

    return MessagesReqDTO(messages)
}
//
//fun List<MessageAttachmentRelation>.toMessageResDto(mustStatus: Constants.StatusMustBe): List<MessageDTO> {
//    return map {
//        MessageDTO(
//            id = it.messageEntity.webId,
//            type = Constants.MessageType.TEXT.type,
//            user = it.contact?.let { contactEntity -> contactEntity.id }?.run { 0 },
//            status = mustStatus.status
//        )
//    }
//}

//fun AttachmentEntity.toAttachmentResDTO(): AttachmentResDTO {
//    return AttachmentResDTO(
//        messageId = this.messageId.toString(),
//        body = this.body,
//        type = this.type,
//        width = 0,
//        height = 0,
//        extension = this.extension,
//        id = this.webId,
//        duration = this.duration,
//        destroy = this.selfDestructionAt.toString()
//    )
//}

fun mappingMessagesDto(
    attachment: NewMessageEventAttachmentRes,
    list: List<NewMessageEventMessageRes>,
    mustStatus: Constants.StatusMustBe
): MessageDTO {

    val message = list.first {
        it.id == attachment.messageId
    }

    return MessageDTO(
        id = attachment.id,
        type = Constants.MessageType.ATTACHMENT.type,
        user = message.userAddressee,
        status = mustStatus.status
    )

}

fun mappingMessagesDtoFrom(
    attachment: AttachmentResDTO,
    list: List<MessageResDTO>,
    mustStatus: Constants.StatusMustBe
): MessageDTO {

    val message = list.first {
        it.id == attachment.messageId
    }

    return MessageDTO(
        id = attachment.id,
        type = Constants.MessageType.ATTACHMENT.type,
        user = message.userAddressee,
        status = mustStatus.status
    )

}

fun MessagesResDTO.extractIdsMessages(): List<String> {
    return messages.filter {
        it.type == Constants.MessageType.TEXT.type
    }.map {
        it.id
    }
}

fun MessagesResDTO.extractIdsAttachments(): List<String> {
    return messages.filter {
        it.type == Constants.MessageType.ATTACHMENT.type
    }.map {
        it.id
    }
}