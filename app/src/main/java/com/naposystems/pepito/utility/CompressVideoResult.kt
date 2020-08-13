package com.naposystems.pepito.utility

import com.naposystems.pepito.entity.message.attachments.Attachment
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope

sealed class CompressVideoResult {

    object Start : CompressVideoResult()

    data class Success(val attachment: Attachment) : CompressVideoResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    ) : CompressVideoResult()

    data class Progress(
        val attachment: Attachment,
        val progress: Long,
        val job: ProducerScope<CompressVideoResult>
    ) : CompressVideoResult()

    data class Complete(val attachment: Attachment) : CompressVideoResult()
}