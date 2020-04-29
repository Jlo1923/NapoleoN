package com.naposystems.pepito.ui.attachmentGalleryFolder

import com.naposystems.pepito.model.attachment.gallery.GalleryResult
import kotlinx.coroutines.flow.Flow

interface IContractAttachmentGalleryFolders {

    interface ViewModel

    interface Repository {
        fun getFolders(): Flow<GalleryResult>
    }
}