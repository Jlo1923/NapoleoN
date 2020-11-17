package com.naposystems.napoleonchat.db.dao.attachment

import com.naposystems.napoleonchat.entity.message.attachments.Attachment

interface AttachmentDataSource {

    fun insertAttachment(attachment: Attachment): Long

    fun insertAttachments(listAttachment: List<Attachment>): List<Long>

    fun updateAttachment(attachment: Attachment)

    suspend fun suspendUpdateAttachment(attachment: Attachment)

    fun updateAttachmentState(webId: String, state: Int)
}