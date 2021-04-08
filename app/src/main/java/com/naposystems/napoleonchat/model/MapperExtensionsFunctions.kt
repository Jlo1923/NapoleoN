package com.naposystems.napoleonchat.model

import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.source.remote.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.utility.Constants

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
        offer = this[Constants.CallKeys.OFFER].toString()

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
            type = Constants.MessageTypeByStatus.MESSAGE.type,
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

    return MessagesReqDTO(
        messages
    )

}

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
        type = Constants.MessageTypeByStatus.ATTACHMENT.type,
        user = message.userAddressee,
        status = mustStatus.status
    )

}

fun MessagesResDTO.extractIdsMessages(): List<String> {
    return messages.filter {
        it.type == Constants.MessageTypeByStatus.MESSAGE.type
    }.map {
        it.id
    }
}

fun MessagesResDTO.extractIdsAttachments(): List<String> {
    return messages.filter {
        it.type == Constants.MessageTypeByStatus.ATTACHMENT.type
    }.map {
        it.id
    }
}