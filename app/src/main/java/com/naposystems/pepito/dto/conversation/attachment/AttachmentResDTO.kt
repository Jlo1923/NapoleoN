package com.naposystems.pepito.dto.conversation.attachment

import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "body") val body: String,
    @Json(name = "message_id") val messageId: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "ext") val extension: String
) {
    companion object {

        fun toListConversationAttachment(
            conversationId: Int,
            listAttachmentsDTO: List<AttachmentResDTO>
        ): List<Attachment> {
            val mutableList: MutableList<Attachment> = ArrayList()

            for (attachmentDTO in listAttachmentsDTO) {
                val attachment = Attachment(
                    0,
                    conversationId,
                    attachmentDTO.id,
                    attachmentDTO.messageId,
                    attachmentDTO.type,
                    attachmentDTO.body,
                    "",
                    Constants.AttachmentOrigin.DOWNLOADED.origin,
                    "",
                    Constants.AttachmentStatus.SENT.status
                )

                attachment.extension = attachmentDTO.extension

                mutableList.add(attachment)
            }

            return mutableList
        }
    }
}