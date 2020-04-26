package com.naposystems.pepito.utility

import com.naposystems.pepito.entity.message.attachments.Attachment

sealed class UploadResult {

    data class Success(val attachment: Attachment) : UploadResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    ) : UploadResult()

    data class Progress(val attachment: Attachment, val progress: Long) : UploadResult()
}