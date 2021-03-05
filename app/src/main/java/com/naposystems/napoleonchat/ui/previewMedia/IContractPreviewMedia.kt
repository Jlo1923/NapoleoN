package com.naposystems.napoleonchat.ui.previewMedia

import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import java.io.File

interface IContractPreviewMedia {

    interface ViewModel {
        fun createTempFile(attachmentEntity: AttachmentEntity)
        fun sentMessageReaded(messageAttachmentRelation: MessageAttachmentRelation)
    }

    interface Repository {
        suspend fun createTempFile(attachmentEntity: AttachmentEntity): File?
        suspend fun sentMessageReaded(messageAndAttachmentRelation: MessageAttachmentRelation)
    }
}