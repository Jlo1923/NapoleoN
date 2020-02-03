package com.naposystems.pepito.db.dao.attachment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import com.naposystems.pepito.entity.message.Attachment

@Dao
interface AttachmentDao {

    @Insert
    fun insertAttachment(listAttachment: List<Attachment>): List<Long>

    @Update
    fun updateAttachments(listAttachment: List<Attachment>)
}