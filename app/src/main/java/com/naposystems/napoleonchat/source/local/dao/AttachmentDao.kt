package com.naposystems.napoleonchat.source.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

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

    @Query(
        "SELECT ${DBConstants.Attachment.COLUMN_ID} " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID}  = :webId"
    )
    fun existAttachmentByWebId(webId: String): Int?

    @Query(
        "SELECT ${DBConstants.Attachment.COLUMN_ID} " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_ID}  = :id"
    )
    fun existAttachmentById(id: String): Int?

    @Query("SELECT * FROM  ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} WHERE ${DBConstants.Attachment.COLUMN_WEB_ID} =:id")
    suspend fun getAttachmentByWebId(id: String): AttachmentEntity?

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID} = :id"
    )
    fun getAttachmentByWebIdLiveData(id: String): LiveData<AttachmentEntity?>

    @Query(
        "SELECT ${DBConstants.Attachment.COLUMN_SELF_DESTRUCTION_AT} " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID} = :webId"
    )
    fun getAttachmentSelfDestructTimeById(webId: String): Int


    @Query(
        "DELETE " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID}  = :webId"
    )
    suspend fun deletedAttachment(webId: String)

    @Query(
        "UPDATE ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "SET ${DBConstants.Attachment.COLUMN_STATUS} = :status, " +
                "${DBConstants.Attachment.COLUMN_UPDATED_AT} = :updateAttachmentStatus, " +
                "${DBConstants.Attachment.COLUMN_TOTAL_SELF_DESTRUCTION_AT} = :totalSelfDestructTime " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID} = :webId"
    )
    fun updateAttachmentStatus(
        webId: String,
        updateAttachmentStatus: Long,
        totalSelfDestructTime: Long,
        status: Int
    )

    @Query(
        "UPDATE ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "SET ${DBConstants.Attachment.COLUMN_SELF_DESTRUCTION_AT} = :selfDestructTime, " +
                "${DBConstants.Attachment.COLUMN_TOTAL_SELF_DESTRUCTION_AT} = 0, " +
                "${DBConstants.Attachment.COLUMN_STATUS} = :status " +
                "WHERE ${DBConstants.Attachment.COLUMN_WEB_ID} = :webId"
    )
    suspend fun updateSelfDestructTimeByAttachments(
        selfDestructTime: Int,
        webId: String,
        status: Int
    )

    @Query(
        "SELECT * " +
                "FROM ${DBConstants.Attachment.TABLE_NAME_ATTACHMENT} " +
                "WHERE ${DBConstants.Attachment.COLUMN_SELF_DESTRUCTION_AT} <> 0 " +
                "AND ${DBConstants.Attachment.COLUMN_STATUS} = 11 " +
                "AND ${DBConstants.Attachment.COLUMN_TOTAL_SELF_DESTRUCTION_AT} < strftime('%s','now')"
    )
    fun getAttachmentsSelfDestructionExpired(): List<AttachmentEntity>


}