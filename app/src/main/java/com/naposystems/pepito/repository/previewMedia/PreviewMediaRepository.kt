package com.naposystems.pepito.repository.previewMedia

import android.content.Context
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.previewMedia.IContractPreviewMedia
import com.naposystems.pepito.utility.FileManager
import java.io.File
import javax.inject.Inject

class PreviewMediaRepository @Inject constructor(private val context: Context) :
    IContractPreviewMedia.Repository {

    override suspend fun createTempFile(attachment: Attachment): File? {
        return FileManager.createTempFileFromEncryptedFile(
            context,
            attachment.type,
            "${attachment.webId}.${attachment.extension}",
            attachment.extension
        )
    }
}