package com.naposystems.pepito.db.dao.attachment

import com.naposystems.pepito.entity.message.attachments.Attachment

interface AttachmentDataSource {

    fun insertAttachment(attachment: Attachment): Long

    fun insertAttachments(listAttachment: List<Attachment>): List<Long>

    fun updateAttachments(attachmentId: Long, webId: String, messageWebId: String, body: String)
}