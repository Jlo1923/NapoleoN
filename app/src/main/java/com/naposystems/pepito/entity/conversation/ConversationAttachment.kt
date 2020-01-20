package com.naposystems.pepito.entity.conversation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.pepito.dto.conversation.message.ConversationAttachmentReqDTO

@Entity(
    tableName = "conversation_attachment", foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class ConversationAttachment(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "conversation_id") val conversationId: Int,
    @ColumnInfo(name = "web_id") val webId: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "uri") val uri: String
) {
    companion object {
        fun toListConversationAttachmentDTO(listConversationAttachment: List<ConversationAttachment>): List<ConversationAttachmentReqDTO> {
            val mutableAttachmentDTO: MutableList<ConversationAttachmentReqDTO> = ArrayList()

            for (conversationAttachment in listConversationAttachment) {
                mutableAttachmentDTO.add(
                    ConversationAttachmentReqDTO(
                        conversationAttachment.type,
                        conversationAttachment.body
                    )
                )
            }

            return mutableAttachmentDTO
        }
    }
}