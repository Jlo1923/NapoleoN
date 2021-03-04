package com.naposystems.napoleonchat.entity

import androidx.room.*

@Entity(
    tableName = "message_not_sent", foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contact_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contact_id"], unique = true)]
)
data class MessageNotSent(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "contact_id") val contactId: Int
)