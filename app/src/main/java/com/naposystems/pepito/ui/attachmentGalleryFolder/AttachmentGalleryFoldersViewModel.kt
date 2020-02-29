package com.naposystems.pepito.ui.attachmentGalleryFolder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.repository.attachmentGalleryFolder.AttachmentGalleryFolderRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttachmentGalleryFoldersViewModel @Inject constructor(private val repository: AttachmentGalleryFolderRepository) :
    ViewModel(), IContractAttachmentGalleryFolders.ViewModel {

    private val _folders = MutableLiveData<List<GalleryFolder>>()
    val folders: LiveData<List<GalleryFolder>>
        get() = _folders

    //region Implementation IContractAttachmentGallery.ViewModel
    override fun loadFolders() {
        viewModelScope.launch {
            _folders.value = repository.getFolders()
        }
    }
    //endregion
}
