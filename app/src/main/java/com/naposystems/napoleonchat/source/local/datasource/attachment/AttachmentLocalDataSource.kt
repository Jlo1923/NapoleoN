package com.naposystems.napoleonchat.source.local.datasource.attachment

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

interface AttachmentLocalDataSource {

    fun insertAttachment(attachmentEntity: AttachmentEntity): Long

    fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long>

    fun updateAttachment(attachmentEntity: AttachmentEntity)

    suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity)

    fun updateAttachmentStatus(webId: String, state: Int)

    suspend fun updateAttachmentStatus(attachmentsWebIds: List<String>, status: Int)

    suspend fun deletedAttachments(attachmentsWebIds: List<String>)

    fun existAttachmentByWebId(id: String): Boolean

    suspend fun getAttachmentByWebId(webId: String): AttachmentEntity?

}