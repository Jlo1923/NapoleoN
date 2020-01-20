package com.naposystems.pepito.dto.conversation.message

import com.naposystems.pepito.entity.conversation.ConversationAttachment
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationAttachmentResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "body") val body: String,
    @Json(name = "message_id") val messageId: String
) {
    companion object {

        fun toListConversationAttachment(
            listAttachmentsDTO: List<ConversationAttachmentResDTO>,
            conversationId: Int,
            listAttachmentsId: List<Long>
        ): List<ConversationAttachment> {
            val mutableList: MutableList<ConversationAttachment> = ArrayList()

            for ((index, attachmentDTO) in listAttachmentsDTO.withIndex()) {
                mutableList.add(
                    ConversationAttachment(
                        listAttachmentsId[index].toInt(),
                        conversationId,
                        attachmentDTO.id,
                        attachmentDTO.messageId,
                        attachmentDTO.type,
                        attachmentDTO.body,
                        ""
                    )
                )
            }

            return mutableList
        }
    }
}