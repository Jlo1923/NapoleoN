package com.naposystems.pepito.dto.conversation.message

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
    @Json(name = "attachments") var attachments: List<AttachmentResDTO> = ArrayList()
) {
    companion object {

        fun toMessageListEntity(
            listMessageResDTO: List<MessageResDTO>,
            isMine: Int
        ): List<Message> {
            val mutableList: MutableList<Message> = ArrayList()

            for (messageRes in listMessageResDTO) {
                mutableList.add(
                    Message(
                        0,
                        messageRes.id,
                        messageRes.body,
                        messageRes.quoted,
                        messageRes.userAddressee,
                        messageRes.updatedAt,
                        messageRes.createdAt,
                        isMine,
                        Constants.MessageStatus.UNREAD.status
                    )
                )
            }

            return mutableList
        }

        fun toMessageEntity(
            messageId: Int,
            messageResDTO: MessageResDTO,
            isMine: Int
        ): Message {

            return Message(
                messageId,
                messageResDTO.id,
                messageResDTO.body,
                messageResDTO.quoted,
                if (isMine == Constants.IsMine.NO.value) messageResDTO.userAddressee else messageResDTO.userDestination,
                messageResDTO.updatedAt,
                messageResDTO.createdAt,
                isMine,
                if (isMine == Constants.IsMine.NO.value) Constants.MessageStatus.UNREAD.status else Constants.MessageStatus.SENT.status
            )
        }
    }
}