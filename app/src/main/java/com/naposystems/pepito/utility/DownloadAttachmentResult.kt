package com.naposystems.pepito.utility

import com.naposystems.pepito.entity.message.attachments.Attachment

sealed class DownloadAttachmentResult {

    data class Success(val attachment: Attachment, val itemPosition: Int) : DownloadAttachmentResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    ) : DownloadAttachmentResult()

    data class Progress(val itemPosition: Int, val progress: Long) : DownloadAttachmentResult()
}