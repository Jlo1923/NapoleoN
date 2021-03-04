package com.naposystems.napoleonchat.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.source.local.DBConstants

@Entity(tableName = DBConstants.Status.TABLE_NAME_STATUS)
data class StatusEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstants.Status.COLUMN_ID) val id: Int,
    @ColumnInfo(name = DBConstants.Status.COLUMN_STATUS) var status: String = "",
    @ColumnInfo(name = DBConstants.Status.COLUMN_CUSTOM_STATUS) val customStatus: String = ""
)