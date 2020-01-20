package com.naposystems.pepito.dto.conversation.message

import com.naposystems.pepito.entity.conversation.Conversation
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "body") val body: String,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "user_sender") val userAddressee: Int,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "attachments") var conversationAttachments: List<ConversationAttachmentResDTO> = ArrayList()
) {
    companion object {

        fun toConversationListEntity(
            listConversationResDTO: List<ConversationResDTO>,
            isMine: Int,
            channelName: String
        ): List<Conversation> {
            val mutableList: MutableList<Conversation> = ArrayList()

            for (conversationRes in listConversationResDTO) {
                mutableList.add(
                    Conversation(
                        0,
                        conversationRes.id,
                        conversationRes.body,
                        conversationRes.quoted,
                        conversationRes.userDestination,
                        conversationRes.userAddressee,
                        conversationRes.updatedAt,
                        conversationRes.createdAt,
                        isMine,
                        channelName
                    )
                )
            }

            return mutableList
        }

        fun toConversationEntity(
            conversationId: Int,
            conversationResDTO: ConversationResDTO,
            isMine: Int,
            channelName: String
        ): Conversation {

            return Conversation(
                conversationId,
                conversationResDTO.id,
                conversationResDTO.body,
                conversationResDTO.quoted,
                conversationResDTO.userDestination,
                conversationResDTO.userAddressee,
                conversationResDTO.updatedAt,
                conversationResDTO.createdAt,
                isMine,
                channelName
            )
        }
    }
}