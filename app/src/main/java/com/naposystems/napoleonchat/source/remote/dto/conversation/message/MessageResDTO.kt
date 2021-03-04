package com.naposystems.napoleonchat.source.remote.dto.conversation.message

import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class
MessageResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "uuid_sender") val webUuid: String?,
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
            messageEntity: MessageEntity?,
            messageResDTO: MessageResDTO,
            isMine: Int
        ): MessageEntity {

            return MessageEntity(
                id = messageEntity?.id ?: 0,
                webId = messageResDTO.id,
                uuid = messageResDTO.webUuid,
                body = messageEntity?.body ?: messageResDTO.body,
                quoted = messageResDTO.quoted,
                contactId = if (isMine == Constants.IsMine.NO.value) messageResDTO.userAddressee else messageResDTO.userDestination,
                updatedAt = messageResDTO.updatedAt,
                createdAt = messageEntity?.createdAt ?: messageResDTO.createdAt,
                isMine = isMine,
                status = if (isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status else Constants.MessageStatus.SENDING.status,
                numberAttachments = messageResDTO.numberAttachments,
                selfDestructionAt = messageResDTO.destroy,
                messageType = messageResDTO.messageType
            )
        }
    }
}