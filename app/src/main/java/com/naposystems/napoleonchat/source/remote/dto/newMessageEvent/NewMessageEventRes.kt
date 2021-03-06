package com.naposystems.napoleonchat.source.remote.dto.newMessageEvent

import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
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
    @Json(name = "uuid_sender") val uuidSender: String,
    @Json(name = "body") val body: String,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "user_sender") val userAddressee: Int,
    @Json(name = "updated_at") val updatedAt: Int,
    @Json(name = "created_at") val createdAt: Int,
    @Json(name = "attachments_finally") var attachments: List<NewMessageEventAttachmentRes> = ArrayList(),
    @Json(name = "destroy") val destroy: Int = -1,
    @Json(name = "number_attachments") val numberAttachments: Int,
    @Json(name = "type_message") val messageType: Int
) {
    fun toMessageEntity(
        isMine: Int
    ): MessageEntity {

        return MessageEntity(
            id = 0,
            webId = this.id,
            uuid = this.uuidSender,
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
    ): MessageEntity {

        return MessageEntity(
            id = 0,
            webId = this.id,
            uuid = this.uuidSender,
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
    @Json(name = "uuid_sender") val uuidSender: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "ext") val extension: String,
    @Json(name = "duration") val duration: Float,
    @Json(name = "destroy") val destroy: String
) {
    companion object {
        fun toListConversationAttachment(
            conversationId: Int,
            listAttachmentsDTO: List<NewMessageEventAttachmentRes>
        ): List<AttachmentEntity> {
            val mutableList: MutableList<AttachmentEntity> = ArrayList()

            for (attachmentDTO in listAttachmentsDTO) {
                val attachment = AttachmentEntity(
                    id = 0,
                    messageId = conversationId,
                    webId = attachmentDTO.id,
                    messageWebId = attachmentDTO.messageId,
                    uuid = attachmentDTO.uuidSender,
                    type = attachmentDTO.type,
                    body = attachmentDTO.body,
                    fileName = "",
                    origin = Constants.AttachmentOrigin.DOWNLOADED.origin,
                    thumbnailUri = "",
                    status = Constants.AttachmentStatus.NOT_DOWNLOADED.status,
                    extension = attachmentDTO.extension,
                    duration = attachmentDTO.duration.toLong(),
                    selfDestructionAt = attachmentDTO.destroy.toInt()
                )
                mutableList.add(attachment)
            }

            return mutableList
        }
    }
}
