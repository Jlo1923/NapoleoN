package com.naposystems.pepito.ui.attachmentGalleryFolder

import com.naposystems.pepito.model.attachment.gallery.GalleryResult
import kotlinx.coroutines.flow.Flow

interface IContractAttachmentGalleryFolders {

    interface ViewModel {
        fun getFolders(isConversation: Boolean)
    }

    interface Repository {
        fun getFolders(isConversation: Boolean): Flow<GalleryResult>
    }
}