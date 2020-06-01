package com.naposystems.pepito.utility

import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.attachments.Attachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import retrofit2.Call

sealed class UploadResult {

    data class StartCall(val attachment: Attachment, val call: Call<AttachmentResDTO>) :
        UploadResult()

    data class Start(val attachment: Attachment, val job: Job) : UploadResult()

    data class Success(val attachment: Attachment) : UploadResult()

    data class Error(
        val attachment: Attachment,
        val message: String,
        val cause: Exception? = null
    ) : UploadResult()

    data class Progress(val attachment: Attachment, val progress: Long, val job: Job) : UploadResult()

    data class Cancel(val attachment: Attachment, val message: Message) : UploadResult()
}