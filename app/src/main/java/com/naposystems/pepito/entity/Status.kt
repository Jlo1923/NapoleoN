package com.naposystems.pepito.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status")
data class Status(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "resource_id") val resourceId: Int,
    @ColumnInfo(name = "custom_status") val customStatus: String
)