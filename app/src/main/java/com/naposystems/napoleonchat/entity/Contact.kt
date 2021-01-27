package com.naposystems.napoleonchat.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
@Entity(tableName = "contact")
data class Contact(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "image_url") var imageUrl: String = "",
    @ColumnInfo(name = "image_url_fake") val imageUrlFake: String = "",
    @ColumnInfo(name = "nickname") val nickname: String = "",
    @ColumnInfo(name = "nickname_fake") val nicknameFake: String = "",
    @ColumnInfo(name = "display_name") var displayName: String,
    @ColumnInfo(name = "display_name_fake") val displayNameFake: String = "",
    @ColumnInfo(name = "status") var status: String = "",
    @ColumnInfo(name = "last_seen") var lastSeen: String = "",
    @ColumnInfo(name = "status_blocked") var statusBlocked: Boolean = false,
    @ColumnInfo(name = "silenced") val silenced: Boolean = false,
    @ColumnInfo(name = "self_destruct_time") val selfDestructTime: Int = -1,
    @ColumnInfo(name = "state_notification") var stateNotification: Boolean = false,
    @ColumnInfo(name = "notification_id") val notificationId: String? = null
) : Parcelable, Serializable {
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

    fun getNickName(): String = if (nicknameFake.isNotEmpty()) nicknameFake else nickname

    fun getName(): String = if (displayNameFake.isNotEmpty()) displayNameFake else displayName

    fun getImage(): String = when {
        imageUrlFake.isNotEmpty() -> imageUrlFake
        imageUrl.isNotEmpty() -> imageUrl
        else -> ""
    }

}