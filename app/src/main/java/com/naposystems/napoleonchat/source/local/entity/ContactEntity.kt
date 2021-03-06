package com.naposystems.napoleonchat.source.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.source.local.DBConstants
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
@Entity(tableName = DBConstants.Contact.TABLE_NAME_CONTACT)
data class ContactEntity(
    @PrimaryKey
    @ColumnInfo(name =DBConstants.Contact.COLUMN_ID) val id: Int,
    @ColumnInfo(name =DBConstants.Contact.COLUMN_IMAGE_URL) var imageUrl: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_IMAGE_URL_FAKE) val imageUrlFake: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_NICKNAME) val nickname: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_NICKNAME_FAKE) val nicknameFake: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_DISPLAY_NAME) var displayName: String,
    @ColumnInfo(name =DBConstants.Contact.COLUMN_DISPLAY_NAME_FAKE) val displayNameFake: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_STATUS) var status: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_LAST_SEEN) var lastSeen: String = "",
    @ColumnInfo(name =DBConstants.Contact.COLUMN_STATUS_BLOCKED) var statusBlocked: Boolean = false,
    @ColumnInfo(name =DBConstants.Contact.COLUMN_SILENCED) val silenced: Boolean = false,
    @ColumnInfo(name =DBConstants.Contact.COLUMN_SELF_DESTRUCT_TIME) val selfDestructTime: Int = -1,
    @ColumnInfo(name =DBConstants.Contact.COLUMN_STATE_NOTIFICATION) var stateNotification: Boolean = false,
    @ColumnInfo(name =DBConstants.Contact.COLUMN_NOTIFICATION_ID) val notificationId: String? = null
) : Parcelable, Serializable {
    @Ignore
    var haveFriendshipRequest: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactEntity

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