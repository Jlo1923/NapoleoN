package com.naposystems.napoleonchat.repository.previewMedia

import android.content.Context
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessagesReadReqDTO
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class PreviewMediaRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageLocalDataSource
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

    override suspend fun sentMessageReaded(messageAndAttachmentRelation: MessageAttachmentRelation) {
        try {
            val response = napoleonApi.sendMessagesRead(
                MessagesReadReqDTO(
                    listOf(messageAndAttachmentRelation.messageEntity.webId)
                )
            )

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.READED.status
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }
}