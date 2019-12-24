package com.naposystems.pepito.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naposystems.pepito.utility.Constants

@Entity(tableName = "conversation")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "web_id") val webId: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "user_destination") val userDestination: Int,
    @ColumnInfo(name = "user_addressee") val userAddressee: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "is_mine") val isMine: Int,
    @ColumnInfo(name = "channel_name") val channelName: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Conversation

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