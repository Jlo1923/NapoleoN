package com.naposystems.pepito.ui.previewMedia

import com.naposystems.pepito.entity.message.attachments.Attachment
import java.io.File

interface IContractPreviewMedia {

    interface ViewModel {
        fun createTempFile(attachment: Attachment)
    }

    interface Repository {
        suspend fun createTempFile(attachment: Attachment): File?
    }
}