package com.naposystems.napoleonchat.source.local.datasource.attachment

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.dao.AttachmentDao
import javax.inject.Inject

class AttachmentLocalDataSourceImp @Inject constructor(
    private val attachmentDao: AttachmentDao
) : AttachmentLocalDataSource {

    override fun insertAttachment(attachmentEntity: AttachmentEntity): Long {
        return attachmentDao.insertAttachment(attachmentEntity)
    }

    override fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long> {
        return attachmentDao.insertAttachments(listAttachmentEntity)
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) {
        attachmentDao.updateAttachment(attachmentEntity)
    }

    override suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity) {
        attachmentDao.suspendUpdateAttachment(attachmentEntity)
    }

    override fun updateAttachmentState(webId: String, state: Int) {
        attachmentDao.updateAttachmentState(webId, state)
    }
}