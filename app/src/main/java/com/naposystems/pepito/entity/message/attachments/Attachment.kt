package com.naposystems.pepito.entity.message.attachments

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.pepito.dto.conversation.message.AttachmentReqDTO
import com.naposystems.pepito.entity.message.Message

@Entity(
    tableName = "attachment", foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "message_id") val messageId: Int,
    @ColumnInfo(name = "web_id") val webId: String,
    @ColumnInfo(name = "message_web_id") val messageWebId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "uri") var uri: String,
    @ColumnInfo(name = "origin") var origin: Int
) {
    companion object {
        fun toListAttachmentDTO(listAttachment: List<Attachment>): List<AttachmentReqDTO> {
            val mutableAttachmentDTO: MutableList<AttachmentReqDTO> = ArrayList()

            for (attachment in listAttachment) {
                mutableAttachmentDTO.add(
                    AttachmentReqDTO(
                        attachment.type,
                        attachment.body
                    )
                )
            }

            return mutableAttachmentDTO
        }

        fun toAttachmentDTO(attachment: Attachment) = AttachmentReqDTO(
            type = attachment.type,
            body = attachment.body
        )
    }
}