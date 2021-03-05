package com.naposystems.napoleonchat.source.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.source.local.DBConstants
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = DBConstants.Quote.TABLE_NAME_QUOTE,
    foreignKeys = [
        //Mensaje
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = [DBConstants.Message.COLUMN_ID],
            childColumns = [DBConstants.Quote.COLUMN_MESSAGE_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        //Mensaje Padre
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = [DBConstants.Message.COLUMN_ID],
            childColumns = [DBConstants.Quote.COLUMN_MESSAGE_PARENT_ID],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.SET_NULL
        )
    ]
)
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstants.Quote.COLUMN_ID) var id: Int,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_MESSAGE_ID) val messageId: Int,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_CONTACT_ID) val contactId: Int,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_BODY) val body: String,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_ATTACHMENT_TYPE) val attachmentType: String,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_THUMBNAIL_URI) val thumbnailUri: String,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_MESSAGE_PARENT_ID) val messageParentId: Int?,
    @ColumnInfo(name = DBConstants.Quote.COLUMN_IS_MINE) val isMine: Int
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuoteEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}