package com.naposystems.pepito.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "contact")
data class Contact(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "image_url_fake") val imageUrlFake: String = "",
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "nickname_fake") val nicknameFake: String = "",
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "display_name_fake") val displayNameFake: String = "",
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "last_seen") val lastSeen: String,
    @ColumnInfo(name = "status_blocked") var statusBlocked: Boolean = false
    @ColumnInfo(name = "silenced") val silenced: Boolean = false
) : Parcelable {
    @Ignore
    var haveFriendshipRequest: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (id != other.id) return false
        if (nickname != other.nickname) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + nickname.hashCode()
        return result
    }


}