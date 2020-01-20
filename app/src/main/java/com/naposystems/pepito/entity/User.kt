package com.naposystems.pepito.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user")
data class User(
    @PrimaryKey @ColumnInfo(name = "firebase_id") val firebaseId: String,
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "display_name") var displayName: String,
    @ColumnInfo(name = "access_pin") val accessPin: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "header_uri") val headerUri: String,
    @ColumnInfo(name = "chat_background") val chatBackground: String
) : Parcelable