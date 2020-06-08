package com.naposystems.pepito.ui.previewMedia

import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import java.io.File

interface IContractPreviewMedia {

    interface ViewModel {
        fun createTempFile(attachment: Attachment)
        fun sentMessageReaded(messageAndAttachment: MessageAndAttachment)
    }

    interface Repository {
        suspend fun createTempFile(attachment: Attachment): File?
        suspend fun sentMessageReaded(messageAndAttachment: MessageAndAttachment)
    }
}