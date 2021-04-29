package com.naposystems.napoleonchat.repository.previewMedia

import android.content.Context
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentItemAttachment
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class PreviewMediaRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val syncManager: SyncManager
) :
    IContractPreviewMedia.Repository {

    override suspend fun createTempFile(attachmentEntity: AttachmentEntity): File? {
        val fileName = when (attachmentEntity.status) {
            Constants.AttachmentStatus.SENT.status,
            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status -> {
                "${attachmentEntity.webId}.${attachmentEntity.extension}"
            }
            else -> attachmentEntity.fileName
        }

        return FileManager.createTempFileFromEncryptedFile(
            context,
            attachmentEntity.type,
            fileName,
            attachmentEntity.extension
        )
    }

    override suspend fun sentMessageReaded(messageAttachmentRelation: MessageAttachmentRelation) {
        try {

            val messagesReqDTO = MessagesReqDTO(
                messages = listOf(
                    MessageDTO(
                        id = messageAttachmentRelation.messageEntity.webId,
                        status = Constants.StatusMustBe.READED.status,
                        type = Constants.MessageType.TEXT.type,
                        user = messageAttachmentRelation.messageEntity.contactId
                    )
                )
            )

            val response = napoleonApi.sendMessagesRead(messagesReqDTO)

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    listOf(messageAttachmentRelation.messageEntity.webId),
                    Constants.MessageStatus.READED.status
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun sentAttachmentAsRead(
        attachment: MultipleAttachmentItemAttachment,
        contactId: Int
    ): Boolean {
        try {
            val messagesReqDTO = createObjectForApi(attachment, contactId)
            val response = napoleonApi.sendMessagesRead(messagesReqDTO)
            if (response.isSuccessful) {
                attachmentLocalDataSource.updateAttachmentStatus(
                    listOf(attachment.webId),
                    Constants.AttachmentStatus.READED.status
                )
                return true
            }
            return false
        } catch (ex: Exception) {
            Timber.e(ex)
            return false
        }
    }

    private fun createObjectForApi(
        attachment: MultipleAttachmentItemAttachment,
        contactId: Int
    ) = MessagesReqDTO(
        messages = listOf(
            MessageDTO(
                id = attachment.webId,
                status = Constants.StatusMustBe.READED.status,
                type = Constants.MessageType.ATTACHMENT.type,
                user = contactId
            )
        )
    )
}