package com.naposystems.pepito.ui.attachmentGalleryFolder

import com.naposystems.pepito.model.attachment.gallery.GalleryFolder

interface IContractAttachmentGalleryFolders {

    interface ViewModel {
        fun loadFolders()
    }

    interface Repository {
        suspend fun getFolders(): List<GalleryFolder>
    }
}