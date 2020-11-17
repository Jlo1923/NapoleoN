package com.naposystems.napoleonchat.ui.attachmentGallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.naposystems.napoleonchat.dataSource.attachmentGallery.GalleryItemDataSource
import com.naposystems.napoleonchat.dataSource.attachmentGallery.GalleryItemDataSourceFactory
import com.naposystems.napoleonchat.dataSource.attachmentGallery.State
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryFolder
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryItem
import com.naposystems.napoleonchat.repository.attachmentGallery.AttachmentGalleryRepository
import javax.inject.Inject

class AttachmentGalleryViewModel @Inject constructor(private val repository: AttachmentGalleryRepository) :
    ViewModel(), IContractAttachmentGallery.ViewModel {

    lateinit var galleryItems: LiveData<PagedList<GalleryItem>>

    private val perPage = 30
    private lateinit var galleryItemDataSourceFactory: GalleryItemDataSourceFactory

    //region Implementation IContractAttachmentGallery.ViewModel
    override fun loadGalleryItemsByFolder(galleryFolder: GalleryFolder) {
        if (!::galleryItems.isInitialized) {
            galleryItemDataSourceFactory =
                GalleryItemDataSourceFactory(repository, galleryFolder.folderName)

            val config = PagedList.Config.Builder()
                .setPageSize(perPage)
                .setInitialLoadSizeHint(perPage)
                .setEnablePlaceholders(false)
                .build()

            galleryItems = LivePagedListBuilder(galleryItemDataSourceFactory, config).build()
        }
    }

    override fun getState(): LiveData<State> =
        Transformations.switchMap<GalleryItemDataSource, State>(
            galleryItemDataSourceFactory.galleryItemDataSourceLiveData, GalleryItemDataSource::state
        )

    //endregion
}
