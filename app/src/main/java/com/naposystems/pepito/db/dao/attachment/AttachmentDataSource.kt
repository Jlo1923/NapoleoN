package com.naposystems.pepito.db.dao.attachment

import com.naposystems.pepito.entity.message.Attachment

interface AttachmentDataSource {

    fun insertAttachment(listAttachment: List<Attachment>): List<Long>

    fun updateAttachments(listAttachment: List<Attachment>)
}