package com.naposystems.pepito.ui.attachmentGalleryFolder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.naposystems.pepito.model.attachment.gallery.GalleryResult
import com.naposystems.pepito.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AttachmentGalleryFoldersViewModel @Inject constructor(repository: AttachmentGalleryFolderRepository) :
    ViewModel(), IContractAttachmentGalleryFolders.ViewModel {

    val folders: LiveData<GalleryResult> =
        repository.getFolders()
            .asLiveData(Dispatchers.IO)
}
