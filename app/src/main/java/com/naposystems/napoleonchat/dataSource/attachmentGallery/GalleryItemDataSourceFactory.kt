package com.naposystems.napoleonchat.dataSource.attachmentGallery

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem
import com.naposystems.napoleonchat.ui.attachmentGallery.IContractAttachmentGallery

class GalleryItemDataSourceFactory constructor(
    private val repository: IContractAttachmentGallery.Repository,
    private val folderName: String
) :
    DataSource.Factory<Int, GalleryItem>() {

    val galleryItemDataSourceLiveData = MutableLiveData<GalleryItemDataSource>()

    override fun create(): DataSource<Int, GalleryItem> {
        val galleryItemDataSource = GalleryItemDataSource(repository, folderName)
        galleryItemDataSourceLiveData.postValue(galleryItemDataSource)
        return galleryItemDataSource
    }
}