package com.naposystems.napoleonchat.ui.attachmentGalleryFolder

import com.naposystems.napoleonchat.model.attachment.gallery.GalleryResult
import kotlinx.coroutines.flow.Flow

interface IContractAttachmentGalleryFolders {

    interface ViewModel {

        /**
         * Get the folders to show the user
         */
        fun getFolders(isConversation: Boolean)
    }

    interface Repository {
        fun getFolders(isConversation: Boolean): Flow<GalleryResult>
    }
}