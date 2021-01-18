package com.naposystems.napoleonchat.utility

import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import kotlinx.coroutines.Job

sealed class DownloadAttachmentResult {

    data class Start(val itemPosition: Int, val job: Job) :
        DownloadAttachmentResult()

    data class Success(val messageAndAttachment: MessageAndAttachment, val itemPosition: Int) :
        DownloadAttachmentResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val itemPosition: Int,
        val cause: Exception? = null
    ) : DownloadAttachmentResult()

    data class Cancel(val messageAndAttachment: MessageAndAttachment, val itemPosition: Int) :
        DownloadAttachmentResult()

    data class Progress(val itemPosition: Int, val progress: Float) : DownloadAttachmentResult()
}