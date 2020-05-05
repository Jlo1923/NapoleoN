package com.naposystems.pepito.ui.attachmentGalleryFolder

import androidx.lifecycle.*
import com.naposystems.pepito.model.attachment.gallery.GalleryResult
import com.naposystems.pepito.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttachmentGalleryFoldersViewModel @Inject constructor(
    private val repository: AttachmentGalleryFolderRepository
) :
    ViewModel(), IContractAttachmentGalleryFolders.ViewModel {

    private val _galleryFolders = MutableLiveData<GalleryResult>()
    val galleryFolders: LiveData<GalleryResult>
        get() = _galleryFolders

    override fun getFolders(isConversation: Boolean) {
        viewModelScope.launch {
            repository.getFolders(isConversation)
                .flowOn(Dispatchers.IO)
                .collect {
                    _galleryFolders.value = it
                }
        }
    }
}
