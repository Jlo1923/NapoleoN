package com.naposystems.pepito.entity.conversation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.pepito.entity.Contact

@Entity(
    tableName = "conversation", foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contact_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "contact_id") val contactId: Int,
    @ColumnInfo(name = "message") var message: String,
    @ColumnInfo(name = "created") var createdAt: Int,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "unreads") var unReads: Int
)