package com.naposystems.pepito.db.dao.attachment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.pepito.entity.message.Attachment

@Dao
interface AttachmentDao {

    @Insert
    fun insertAttachment(listAttachment: List<Attachment>): List<Long>

    @Query("UPDATE attachment SET web_id=:webId, message_web_id=:messageWebId, body=:body WHERE id=:attachmentId")
    fun updateAttachments(attachmentId: Long, webId: String, messageWebId: String, body: String)
}