package com.naposystems.pepito.repository.previewMedia

import android.content.Context
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.message.MessageLocalDataSource
import com.naposystems.pepito.dto.conversation.message.MessagesReadReqDTO
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.previewMedia.IContractPreviewMedia
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.webService.NapoleonApi
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
            "${attachment.webId}.${attachment.extension}",
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