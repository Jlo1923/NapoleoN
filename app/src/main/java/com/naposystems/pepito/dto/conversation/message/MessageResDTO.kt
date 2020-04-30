package com.naposystems.pepito.dto.conversation.message

import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessageResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "body") val body: String,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "user_sender") val userAddressee: Int,
    @Json(name = "updated_at") val updatedAt: Int,
    @Json(name = "created_at") val createdAt: Int,
    @Json(name = "attachments") var attachments: List<AttachmentResDTO> = ArrayList(),
    @Json(name = "destroy") val destroy: Int = -1,
    @Json(name = "number_attachments") val numberAttachments: Int,
    @Json(name = "type_message") val messageType: Int
    ) {
    companion object {
        fun toMessageEntity(
            message: Message?,
            messageResDTO: MessageResDTO,
            isMine: Int
        ): Message {

            return Message(
                id = message?.id ?: 0,
                webId = messageResDTO.id,
                body = messageResDTO.body,
                quoted = messageResDTO.quoted,
                contactId = if (isMine == Constants.IsMine.NO.value) messageResDTO.userAddressee else messageResDTO.userDestination,
                updatedAt = messageResDTO.updatedAt,
                createdAt = message?.createdAt ?: messageResDTO.createdAt,
                isMine = isMine,
                status = if (isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status else Constants.MessageStatus.SENT.status,
                numberAttachments = messageResDTO.numberAttachments,
                selfDestructionAt = messageResDTO.destroy,
                messageType = messageResDTO.messageType
            )
        }
    }
}