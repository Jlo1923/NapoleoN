package com.naposystems.napoleonchat.service.download.model

import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import kotlinx.coroutines.Job

sealed class DownloadAttachmentResult {

    data class Start(
        val itemPosition: Int,
        val job: Job
    ) : DownloadAttachmentResult()

    data class Success(
        val messageAndAttachmentRelation: MessageAttachmentRelation,
        val itemPosition: Int
    ) : DownloadAttachmentResult()

    data class Error(
        val attachmentEntity: AttachmentEntity,
        val message: String,
        val itemPosition: Int,
        val cause: Exception? = null
    ) : DownloadAttachmentResult()

    data class Cancel(
        val messageAndAttachmentRelation: MessageAttachmentRelation,
        val itemPosition: Int
    ) : DownloadAttachmentResult()

    data class Progress(
        val itemPosition: Int,
        val progress: Float
    ) : DownloadAttachmentResult()
}