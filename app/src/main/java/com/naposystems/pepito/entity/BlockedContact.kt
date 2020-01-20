package com.naposystems.pepito.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_contacts")
data class BlockedContact(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    @ColumnInfo(name = "nickname") val nickname: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "last_seen") val lastSeen: String
)