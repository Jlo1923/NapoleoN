package com.naposystems.napoleonchat.ui.attachmentGallery

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.dataSource.attachmentGallery.State
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryFolder
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem

interface IContractAttachmentGallery {

    interface ViewModel {
        fun loadGalleryItemsByFolder(galleryFolder: GalleryFolder)
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