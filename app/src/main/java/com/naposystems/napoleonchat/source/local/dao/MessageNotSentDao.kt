package com.naposystems.napoleonchat.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.MessageNotSentEntity

@Dao
interface MessageNotSentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageNotSent(messageNotSentEntity: MessageNotSentEntity)

    @Query(
        "DELETE " +
                "FROM ${DBConstants.MessageNotSent.TABLE_NAME_MESSAGE_NOT_SENT} " +
                "WHERE ${DBConstants.MessageNotSent.COLUMN_CONTACT_ID} = :contactId"
    )
    fun deleteMessageNotSentByContact(contactId: Int)

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.MessageNotSent.TABLE_NAME_MESSAGE_NOT_SENT} " +
                "WHERE ${DBConstants.MessageNotSent.COLUMN_CONTACT_ID} = :contactId " +
                "LIMIT 1"
    )
    fun getMessageNotSentByContact(contactId: Int): MessageNotSentEntity
}