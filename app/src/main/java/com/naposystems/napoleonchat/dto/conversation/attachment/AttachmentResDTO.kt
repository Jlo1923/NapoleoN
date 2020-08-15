package com.naposystems.napoleonchat.dto.conversation.attachment

import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentResDTO(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "body") val body: String,
    @Json(name = "type") val type: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "ext") val extension: String,
    @Json(name = "id") val id: String,
    @Json(name = "duration") val duration: Long
) {
    companion object {

        fun toListConversationAttachment(
            conversationId: Int,
            listAttachmentsDTO: List<AttachmentResDTO>
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