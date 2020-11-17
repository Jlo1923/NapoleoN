package com.naposystems.napoleonchat.utility

import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import kotlinx.coroutines.channels.ProducerScope

sealed class UploadResult {

    data class Start(val attachment: Attachment, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Success(val attachment: Attachment) : UploadResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    ) : UploadResult()

    data class CompressProgress(val attachment: Attachment, val progress: Float, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Progress(val attachment: Attachment, val progress: Float, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Complete(val attachment: Attachment) : UploadResult()
}