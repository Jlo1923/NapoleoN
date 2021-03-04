package com.naposystems.napoleonchat.source.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.source.local.DBConstants
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = DBConstants.User.TABLE_NAME_USER)
data class UserEntity(
    @PrimaryKey @ColumnInfo(name = DBConstants.User.COLUMN_FIREBASE_ID) val firebaseId: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_ID) val id: Int,
    @ColumnInfo(name = DBConstants.User.COLUMN_NICKNAME) val nickname: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_DISPLAY_NAME) var displayName: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_ACCESS_PIN) val accessPin: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_IMAGE_URL) val imageUrl: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_STATUS) var status: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_HEADER_URI) var headerUri: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_CHAT_BACKGROUND) val chatBackground: String,
    @ColumnInfo(name = DBConstants.User.COLUMN_TYPE) val type: Int,
    @ColumnInfo(name = DBConstants.User.COLUMN_CREATED_AT) val createAt: Long
) : Parcelable