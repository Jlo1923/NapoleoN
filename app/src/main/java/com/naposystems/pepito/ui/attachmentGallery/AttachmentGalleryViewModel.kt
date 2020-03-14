package com.naposystems.pepito.ui.attachmentGallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.naposystems.pepito.dataSource.attachmentGallery.GalleryItemDataSource
import com.naposystems.pepito.dataSource.attachmentGallery.GalleryItemDataSourceFactory
import com.naposystems.pepito.dataSource.attachmentGallery.State
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.repository.attachmentGallery.AttachmentGalleryRepository
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
