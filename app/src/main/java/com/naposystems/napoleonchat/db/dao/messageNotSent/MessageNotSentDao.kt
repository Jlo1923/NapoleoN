package com.naposystems.napoleonchat.db.dao.messageNotSent

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naposystems.napoleonchat.entity.MessageNotSent

@Dao
interface MessageNotSentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessageNotSent(messageNotSent: MessageNotSent)

    @Query("DELETE FROM message_not_sent WHERE contact_id=:contactId")
    fun deleteMessageNotSentByContact(contactId: Int)

    @Query("SELECT * FROM message_not_sent WHERE contact_id=:contactId LIMIT 1")
    fun getMessageNotSentByContact(contactId: Int): MessageNotSent
}