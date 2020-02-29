package com.naposystems.pepito.ui.attachmentGallery

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dataSource.attachmentGallery.State
import com.naposystems.pepito.model.attachment.gallery.GalleryItem

interface IContractAttachmentGallery {

    interface ViewModel {
        fun loadGalleryItemsByFolder(folderName: String)
        fun getState(): LiveData<State>
    }

    interface Repository {
        suspend fun queryGalleryItemsByFolder(
            page: Int,
            perPage: Int,
            folderName: String
        ): List<GalleryItem>
    }
}