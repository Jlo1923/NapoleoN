package com.naposystems.pepito.db.dao.attachment

import com.naposystems.pepito.entity.message.attachments.Attachment
import javax.inject.Inject

class AttachmentLocalDataSource @Inject constructor(
    private val attachmentDao: AttachmentDao
) : AttachmentDataSource {

    override fun insertAttachment(attachment: Attachment): Long {
        return attachmentDao.insertAttachment(attachment)
    }

    override fun insertAttachments(listAttachment: List<Attachment>): List<Long> {
        return attachmentDao.insertAttachments(listAttachment)
    }

    override fun updateAttachment(attachment: Attachment) {
        attachmentDao.updateAttachment(attachment)
    }

    override suspend fun suspendUpdateAttachment(attachment: Attachment) {
        attachmentDao.suspendUpdateAttachment(attachment)
    }

    override fun updateAttachmentState(webId: String, state: Int) {
        attachmentDao.updateAttachmentState(webId, state)
    }
}