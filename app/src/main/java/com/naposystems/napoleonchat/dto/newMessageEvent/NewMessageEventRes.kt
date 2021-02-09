package com.naposystems.napoleonchat.dto.newMessageEvent

import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewMessageEventRes(
    @Json(name = "data") val data: NewMessageDataEventRes
)

@JsonClass(generateAdapter = true)
data class NewMessageDataEventRes(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "contact_id") val contactId: Int,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class NewMessageEventMessageRes(
    @Json(name = "id") val id: String,
    @Json(name = "uuid_sender") val webUuid: String?,
    @Json(name = "body") val body: String,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "user_sender") val userAddressee: Int,
    @Json(name = "updated_at") val updatedAt: Int,
    @Json(name = "created_at") val createdAt: Int,
    @Json(name = "attachments") var attachments: List<NewMessageEventAttachmentRes> = ArrayList(),
    @Json(name = "destroy") val destroy: Int = -1,
    @Json(name = "number_attachments") val numberAttachments: Int,
    @Json(name = "type_message") val messageType: Int
) {
    fun toMessageEntity(
        isMine: Int
    ): Message {

        return Message(
            id = 0,
            webId = this.id,
            uuid = this.webUuid,
            body = this.body,
            quoted = this.quoted,
            contactId = if (isMine == Constants.IsMine.NO.value) this.userAddressee else this.userDestination,
            updatedAt = this.updatedAt,
            createdAt = this.createdAt,
            isMine = isMine,
            status = if (isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status else Constants.MessageStatus.SENDING.status,
            numberAttachments = this.numberAttachments,
            selfDestructionAt = this.destroy,
            messageType = this.messageType
        )
    }

    fun toMessageEntity(
        isMine: Int,
        cypher: Boolean
    ): Message {

        return Message(
            id = 0,
            webId = this.id,
            uuid = this.webUuid,
            body = this.body,
            quoted = this.quoted,
            contactId = if (isMine == Constants.IsMine.NO.value) this.userAddressee else this.userDestination,
            updatedAt = this.updatedAt,
            createdAt = this.createdAt,
            isMine = isMine,
            status = if (isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status else Constants.MessageStatus.SENDING.status,
            numberAttachments = this.numberAttachments,
            selfDestructionAt = this.destroy,
            messageType = this.messageType,
            cypher = cypher
        )
    }
}

@JsonClass(generateAdapter = true)
data class NewMessageEventAttachmentRes(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "body") val body: String,
    @Json(name = "message_id") val messageId: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "ext") val extension: String,
    @Json(name = "duration") val duration: Long
) {
    companion object {
        fun toListConversationAttachment(
            conversationId: Int,
            listAttachmentsDTO: List<NewMessageEventAttachmentRes>
        ): List<Attachment> {
            val mutableList: MutableList<Attachment> = ArrayList()

            for (attachmentDTO in listAttachmentsDTO) {
                val attachment = Attachment(
                    id = 0,
                    messageId = conversationId,
                    webId = attachmentDTO.id,
                    messageWebId = attachmentDTO.messageId,
                    type = attachmentDTO.type,
                    body = attachmentDTO.body,
                    fileName = "",
                    origin = Constants.AttachmentOrigin.DOWNLOADED.origin,
                    thumbnailUri = "",
                    status = Constants.AttachmentStatus.NOT_DOWNLOADED.status,
                    extension = attachmentDTO.extension,
                    duration = attachmentDTO.duration
                )

                mutableList.add(attachment)
            }

            return mutableList
        }
    }
}
