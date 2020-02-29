package com.naposystems.pepito.dataSource.attachmentGallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.IContractAttachmentGallery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

enum class State {
    DONE, LOADING, ERROR
}

class GalleryItemDataSource constructor(
    private val repository: IContractAttachmentGallery.Repository,
    private val folderName: String
) :
    PageKeyedDataSource<Int, GalleryItem>() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, GalleryItem>
    ) {
        updateState(State.LOADING)
        coroutineScope.launch {
            try {
                val galleryItems = repository.queryGalleryItemsByFolder(
                    page = 0,
                    perPage = params.requestedLoadSize,
                    folderName = folderName
                )
                updateState(State.DONE)
                Timber.d("Page: 0, perPage: ${params.requestedLoadSize}")
                callback.onResult(galleryItems, null, 1)
            } catch (e: Exception) {
                updateState(State.ERROR)
                Timber.e(e)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GalleryItem>) {
        updateState(State.LOADING)
        coroutineScope.launch {
            try {
                val galleryItems = repository.queryGalleryItemsByFolder(
                    page = params.key,
                    perPage = params.requestedLoadSize,
                    folderName = folderName
                )
                updateState(State.DONE)
                Timber.d("Page: ${params.key}, perPage: ${params.requestedLoadSize}")
                callback.onResult(galleryItems, params.key + 1)
            } catch (e: Exception) {
                updateState(State.ERROR)
                Timber.e(e)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GalleryItem>) {

    }

    private fun updateState(state: State) {
        _state.postValue(state)
    }
}