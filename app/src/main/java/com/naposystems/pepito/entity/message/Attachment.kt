package com.naposystems.pepito.entity.message

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.pepito.dto.conversation.message.AttachmentReqDTO

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
    @ColumnInfo(name = "uri") val uri: String
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
    }
}