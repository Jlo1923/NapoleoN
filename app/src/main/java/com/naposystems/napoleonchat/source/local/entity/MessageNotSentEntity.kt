package com.naposystems.napoleonchat.source.local.entity

import androidx.room.*
import com.naposystems.napoleonchat.source.local.DBConstants

@Entity(
    tableName = DBConstants.MessageNotSent.TABLE_NAME_MESSAGE_NOT_SENT,
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = [DBConstants.Contact.COLUMN_ID],
            childColumns = [DBConstants.MessageNotSent.COLUMN_CONTACT_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(
        value = [DBConstants.MessageNotSent.COLUMN_CONTACT_ID],
        unique = true
    )]
)
data class MessageNotSentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstants.MessageNotSent.COLUMN_ID) val id: Int,
    @ColumnInfo(name = DBConstants.MessageNotSent.COLUMN_MESSAGE) val message: String,
    @ColumnInfo(name = DBConstants.MessageNotSent.COLUMN_CONTACT_ID) val contactId: Int
)