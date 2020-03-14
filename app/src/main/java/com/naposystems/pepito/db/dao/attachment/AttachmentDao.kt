package com.naposystems.pepito.db.dao.attachment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import com.naposystems.pepito.entity.message.attachments.Attachment

@Dao
interface AttachmentDao {

    @Insert
    fun insertAttachment(attachment: Attachment): Long

    @Insert
    fun insertAttachments(listAttachment: List<Attachment>): List<Long>

    @Update
    fun updateAttachment(attachment: Attachment)
}