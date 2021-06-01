package com.naposystems.napoleonchat.source.local.entity

import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus
import com.naposystems.napoleonchat.utility.FileManager
import kotlinx.android.parcel.Parcelize

/**
 * El [status] hace referencia al enumerable [AttachmentStatus]
 */
@Parcelize
@Entity(
    tableName = DBConstants.Attachment.TABLE_NAME_ATTACHMENT,
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = [DBConstants.Message.COLUMN_ID],
            childColumns = [DBConstants.Attachment.COLUMN_MESSAGE_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_ID) var id: Int,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_MESSAGE_ID) var messageId: Int,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_WEB_ID) var webId: String,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_MESSAGE_WEB_ID) var messageWebId: String,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_TYPE) val type: String,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_BODY) var body: String,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_FILENAME) var fileName: String,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_ORIGIN) var origin: Int,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_THUMBNAIL_URI) var thumbnailUri: String = "",
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_STATUS) var status: Int,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_EXTENSION) var extension: String = "",
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_DURATION) var duration: Long = 0,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_IS_COMPRESSED) var isCompressed: Boolean = false,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_SELF_DESTRUCTION_AT) var selfDestructionAt: Int = -1,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_TOTAL_SELF_DESTRUCTION_AT) var totalSelfDestructionAt: Int = 0,
    @ColumnInfo(name = DBConstants.Attachment.COLUMN_UPDATED_AT) var updatedAt: Int = 0
) : Parcelable {

    fun deleteFile(context: Context) {
        FileManager.deleteAttachmentEncryptedFile(context, this)
        FileManager.deleteAttachmentFile(context, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentEntity

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

    fun mustBeCompressed(): Boolean {
        return type == Constants.AttachmentType.VIDEO.type && isCompressed.not()
    }

    fun isSent(): Boolean = status == AttachmentStatus.SENT.status
    fun isSending(): Boolean = status == AttachmentStatus.SENDING.status
    fun isReceived(): Boolean = status == AttachmentStatus.RECEIVED.status
    fun isReaded(): Boolean = status == AttachmentStatus.READED.status
    fun isDownloadComplete(): Boolean = status == AttachmentStatus.DOWNLOAD_COMPLETE.status
    fun isCancelUpload(): Boolean = status == AttachmentStatus.UPLOAD_CANCEL.status
    fun isError(): Boolean = status == AttachmentStatus.ERROR.status
    fun isNotDownloaded(): Boolean = status == AttachmentStatus.NOT_DOWNLOADED.status
    fun isDownloadCancel(): Boolean = status == AttachmentStatus.DOWNLOAD_CANCEL.status

    fun isTypeVideo(): Boolean = type == Constants.AttachmentType.VIDEO.type

}