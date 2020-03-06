package com.naposystems.pepito.dto.conversation.message

import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachmentResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "body") val body: String,
    @Json(name = "message_id") val messageId: String
) {
    companion object {

        fun toListConversationAttachment(
            conversationId: Int,
            listAttachmentsDTO: List<AttachmentResDTO>
        ): List<Attachment> {
            val mutableList: MutableList<Attachment> = ArrayList()

            for (attachmentDTO in listAttachmentsDTO) {
                mutableList.add(
                    Attachment(
                        0,
                        conversationId,
                        attachmentDTO.id,
                        attachmentDTO.messageId,
                        attachmentDTO.type,
                        attachmentDTO.body,
                        "",
                        Constants.AttachmentOrigin.DOWNLOADED.origin
                    )
                )
            }

            return mutableList
        }
    }
}