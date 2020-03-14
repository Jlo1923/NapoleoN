package com.naposystems.pepito.entity.message

import androidx.room.*
import com.naposystems.pepito.entity.Contact

@Entity(
    tableName = "message", foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contact_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "web_id") val webId: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "quoted") val quoted: String,
    @ColumnInfo(name = "contact_id") val contactId: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Int,
    @ColumnInfo(name = "created_at") val createdAt: Int,
    @ColumnInfo(name = "is_mine") val isMine: Int,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "is_selected") val isSelected: Boolean = false,
    @ColumnInfo(name = "self_destruction_at") val selfDestructionAt: Int = -1,
    @ColumnInfo(name = "total_self_destruction_at") val totalSelfDestructionAt: Int = 0
    ) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (webId != other.webId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + webId.hashCode()
        return result
    }

}