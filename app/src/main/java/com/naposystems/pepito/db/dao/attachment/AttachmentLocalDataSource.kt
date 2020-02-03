package com.naposystems.pepito.db.dao.attachment

import com.naposystems.pepito.entity.message.Attachment
import javax.inject.Inject

class AttachmentLocalDataSource @Inject constructor(
    private val attachmentDao: AttachmentDao
) : AttachmentDataSource {

    override fun insertAttachment(listAttachment: List<Attachment>): List<Long> {
        return attachmentDao.insertAttachment(listAttachment)
    }

    override fun updateAttachments(listAttachment: List<Attachment>) {
        attachmentDao.updateAttachments(listAttachment)
    }
}