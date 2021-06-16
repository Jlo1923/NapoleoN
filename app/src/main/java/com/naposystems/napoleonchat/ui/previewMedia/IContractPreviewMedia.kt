package com.naposystems.napoleonchat.ui.previewMedia

import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentItemAttachment
import java.io.File

interface IContractPreviewMedia {

    interface ViewModel {
        fun createTempFile(attachmentEntity: AttachmentEntity)
        fun sentMessageReaded(messageAttachmentRelation: MessageAttachmentRelation)
    }

    interface Repository {

        suspend fun createTempFile(attachmentEntity: AttachmentEntity): File?

        suspend fun sentMessageReaded(messageAndAttachmentRelation: MessageAttachmentRelation)

        suspend fun sentAttachmentAsRead(
            attachment: MultipleAttachmentItemAttachment,
            contactId: Int
        ): Boolean

        suspend fun getAttachmentById(webId: String): AttachmentEntity?

        fun removePendingUris()

    }
}