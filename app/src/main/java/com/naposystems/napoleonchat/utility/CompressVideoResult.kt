package com.naposystems.napoleonchat.utility

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import kotlinx.coroutines.channels.ProducerScope

sealed class CompressVideoResult {

    object Start : CompressVideoResult()

    data class Success(val attachmentEntity: AttachmentEntity) : CompressVideoResult()

    data class Error(
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val cause: Exception? = null
    ) : CompressVideoResult()

    data class Progress(
        val attachmentEntity: AttachmentEntity,
        val progress: Long,
        val job: ProducerScope<CompressVideoResult>
    ) : CompressVideoResult()

    data class Complete(val attachmentEntity: AttachmentEntity) : CompressVideoResult()
}