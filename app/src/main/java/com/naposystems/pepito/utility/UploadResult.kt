package com.naposystems.pepito.utility

import com.naposystems.pepito.entity.message.attachments.Attachment
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope

sealed class UploadResult {

    data class Start(val attachment: Attachment, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Success(val attachment: Attachment) : UploadResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    ) : UploadResult()

    data class CompressProgress(val attachment: Attachment, val progress: Long, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Progress(val attachment: Attachment, val progress: Long, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Complete(val attachment: Attachment) : UploadResult()
}