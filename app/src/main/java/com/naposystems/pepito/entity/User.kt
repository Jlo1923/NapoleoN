package com.naposystems.pepito.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey @ColumnInfo(name = "firebase_id") val firebaseId: String,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "access_pin") val accessPin: String
)