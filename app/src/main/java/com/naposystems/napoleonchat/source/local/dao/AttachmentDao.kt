package com.naposystems.napoleonchat.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

@Dao
interface AttachmentDao {

    @Insert
    fun insertAttachment(attachmentEntity: AttachmentEntity): Long

    @Insert
    fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long>

    @Update
    fun updateAttachment(attachmentEntity: AttachmentEntity)

    @Update
    suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity)

    @Query(
        "UPDATE ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "SET ${DBConstants.Attachment.COLUMN_STATUS} = :state " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID} = :webId"
    )
    fun updateAttachmentState(webId: String, state: Int)
}