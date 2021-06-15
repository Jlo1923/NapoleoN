package com.naposystems.napoleonchat.service.multiattachment.contract

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IContractMultipleUpload {

    interface Repository {

        fun uploadAttachment(
            attachmentEntity: AttachmentEntity,
            messageEntity: MessageEntity
        )

        fun cancelUpload()

        fun updateMessage(messageEntity: MessageEntity)

        suspend fun compressVideo(
            attachmentEntity: AttachmentEntity,
            srcFile: File,
            destFile: File,
            job: CoroutineScope
        ): Flow<VideoCompressResult>

        /**
         *
         * Antes de tomar un nuevo attachment, debemos validar el padre del attachment que estabamos tratando
         * debemos validar si todos los attachments del mensaje padre fueron tratados como SENT
         * de ser asi, el mensaje padre debemos marcarlos como SENT
         *
         */
        fun verifyMustMarkMessageAsSent()

        fun updateAttachment(attachmentEntity: AttachmentEntity)

        fun tryMarkAttachmentsInMessageAsError(messageEntity: MessageEntity)

    }

}