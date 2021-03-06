package com.naposystems.napoleonchat.source.local.datasource.attachment

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity

interface AttachmentLocalDataSource {

    fun insertAttachment(attachmentEntity: AttachmentEntity): Long

    fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long>

    fun updateAttachment(attachmentEntity: AttachmentEntity)

    suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity)

    fun updateAttachmentState(webId: String, state: Int)
}