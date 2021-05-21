package com.naposystems.napoleonchat.ui.napoleonKeyboardGif

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giphy.sdk.core.models.Media
import com.naposystems.napoleonchat.repository.napoleonKeyboardGif.NapoleonKeyboardGifRepository
import com.naposystems.napoleonchat.utility.DownloadFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class NapoleonKeyboardGifViewModel @Inject constructor(
    private val repository: NapoleonKeyboardGifRepository
) : ViewModel() {

    private val _downloadProgress = MutableLiveData<DownloadFileResult>()
    val downloadAttachmentProgress: LiveData<DownloadFileResult>
        get() = _downloadProgress

    fun downloadGif(mediaGif: Media) {
        viewModelScope.launch {
            mediaGif.images.original?.gifUrl?.let { gifUrl ->
                repository.downloadGif(gifUrl)
                    .flowOn(Dispatchers.IO)
                    .collect {
                        _downloadProgress.value = it
                    }
            }
        }
    }
}