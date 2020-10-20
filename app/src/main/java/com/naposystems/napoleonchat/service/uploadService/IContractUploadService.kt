package com.naposystems.napoleonchat.service.uploadService

import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.UploadResult
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IContractUploadService {

    interface Repository {
        fun uploadAttachment(
            attachment: Attachment,
            message: Message
        )

        fun updateAttachment(attachment: Attachment)
        fun updateMessage(message: Message)
        suspend fun compressVideo(
            attachment: Attachment,
            srcFile: File,
            destFile: File,
            job: CoroutineScope
        ): Flow<VideoCompressResult>
    }
}