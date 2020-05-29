package com.naposystems.pepito.entity.message.attachments

import android.content.Context
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.utility.Constants.AttachmentStatus
import com.naposystems.pepito.utility.FileManager
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
    @ColumnInfo(name = "uri") var uri: String,
    @ColumnInfo(name = "origin") var origin: Int,
    @ColumnInfo(name = "thumbnail_uri") var thumbnailUri: String = "",
    @ColumnInfo(name = "status") var status: Int,
    @ColumnInfo(name = "extension") var extension: String = ""
) : Parcelable {

    fun deleteFile(context: Context) {
        FileManager.deleteAttachmentEncryptedFile(context, this)
        FileManager.deleteAttachmentFile(context, this)
    }
}