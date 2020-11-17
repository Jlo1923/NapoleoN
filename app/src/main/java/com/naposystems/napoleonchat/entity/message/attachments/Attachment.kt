package com.naposystems.napoleonchat.entity.message.attachments

import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus
import com.naposystems.napoleonchat.utility.FileManager
import kotlinx.android.parcel.Parcelize

/**
 * El [status] hace referencia al enumerable [AttachmentStatus]
 */
@Parcelize
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
    @ColumnInfo(name = "message_id") var messageId: Int,
    @ColumnInfo(name = "web_id") var webId: String,
    @ColumnInfo(name = "message_web_id") var messageWebId: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "filename") var fileName: String,
    @ColumnInfo(name = "origin") var origin: Int,
    @ColumnInfo(name = "thumbnail_uri") var thumbnailUri: String = "",
    @ColumnInfo(name = "status") var status: Int,
    @ColumnInfo(name = "extension") var extension: String = "",
    @ColumnInfo(name = "duration") var duration: Long = 0,
    @ColumnInfo(name = "is_compressed") var isCompressed: Boolean = false
) : Parcelable {

    fun deleteFile(context: Context) {
        FileManager.deleteAttachmentEncryptedFile(context, this)
        FileManager.deleteAttachmentFile(context, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (id != other.id) return false
        if (messageId != other.messageId) return false
        if (webId != other.webId) return false
        if (messageWebId != other.messageWebId) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + messageId
        result = 31 * result + webId.hashCode()
        result = 31 * result + messageWebId.hashCode()
        result = 31 * result + status
        return result
    }

    /*override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (id != other.id) return false
        if (messageId != other.messageId) return false
        if (webId != other.webId) return false
        if (messageWebId != other.messageWebId) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }*/


}