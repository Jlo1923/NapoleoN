package com.naposystems.pepito.entity.message

import androidx.room.*
import com.naposystems.pepito.entity.Contact

@Entity(
    tableName = "message", foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["user_addressee"],
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
    @ColumnInfo(name = "user_addressee") val userAddressee: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Int,
    @ColumnInfo(name = "created_at") val createdAt: Int,
    @ColumnInfo(name = "is_mine") val isMine: Int,
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "is_selected") val isSelected:  Boolean = false
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