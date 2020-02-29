package com.naposystems.pepito.dataSource.attachmentGallery

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.IContractAttachmentGallery

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