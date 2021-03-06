package com.naposystems.napoleonchat.service.uploadService

import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IContractUploadService {

    interface Repository {
        fun uploadAttachment(
            attachmentEntity: AttachmentEntity,
            messageEntity: MessageEntity
        )
        fun cancelUpload()
        fun updateAttachment(attachmentEntity: AttachmentEntity)
        fun updateMessage(messageEntity: MessageEntity)
        suspend fun compressVideo(
            attachmentEntity: AttachmentEntity,
            srcFile: File,
            destFile: File,
            job: CoroutineScope
        ): Flow<VideoCompressResult>
    }
}