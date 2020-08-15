package com.naposystems.napoleonchat.repository.previewMedia

import android.content.Context
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.dto.conversation.message.MessagesReadReqDTO
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.ui.previewMedia.IContractPreviewMedia
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class PreviewMediaRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val messageLocalDataSource: MessageDataSource
) :
    IContractPreviewMedia.Repository {

    override suspend fun createTempFile(attachment: Attachment): File? {
        return FileManager.createTempFileFromEncryptedFile(
            context,
            attachment.type,
            if (attachment.status == Constants.AttachmentStatus.SENT.status) "${attachment.webId}.${attachment.extension}" else attachment.fileName,
            attachment.extension
        )
    }

    override suspend fun sentMessageReaded(messageAndAttachment: MessageAndAttachment) {
        try {
            val response = napoleonApi.sendMessagesRead(
                MessagesReadReqDTO(
                    listOf(messageAndAttachment.message.webId)
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