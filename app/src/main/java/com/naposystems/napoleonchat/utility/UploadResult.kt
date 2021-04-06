package com.naposystems.napoleonchat.utility

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import kotlinx.coroutines.channels.ProducerScope

sealed class UploadResult {

    data class Start(val attachmentEntity: AttachmentEntity, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Success(val attachmentEntity: AttachmentEntity) : UploadResult()

    data class Error(
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val cause: Exception? = null
    ) : UploadResult()

    data class CompressProgress(val attachmentEntity: AttachmentEntity, val progress: Float, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Progress(val attachmentEntity: AttachmentEntity, val progress: Float, val job: ProducerScope<UploadResult>) : UploadResult()

    data class Complete(val attachmentEntity: AttachmentEntity) : UploadResult()
}