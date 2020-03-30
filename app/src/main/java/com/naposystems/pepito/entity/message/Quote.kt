package com.naposystems.pepito.entity.message

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "quote", foreignKeys = [
        ForeignKey(
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(//Mensaje original full hd 4k
            entity = Message::class,
            parentColumns = ["id"],
            childColumns = ["message_parent_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.SET_NULL
        )
    ]
)
data class Quote(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "message_id") val messageId: Int,
    @ColumnInfo(name = "contact_id") val contactId: Int,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "attachment_type") val attachmentType: String,
    @ColumnInfo(name = "thumbnail_uri") val thumbnailUri: String,
    @ColumnInfo(name = "message_parent_id") val messageParentId: Int?,
    @ColumnInfo(name = "is_mine") val isMine: Int
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Quote

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}