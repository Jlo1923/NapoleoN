package com.naposystems.pepito.ui.napoleonKeyboardGif

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giphy.sdk.core.models.Media
import com.naposystems.pepito.utility.DownloadFileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class NapoleonKeyboardGifViewModel @Inject constructor(
    private val repository: IContractNapoleonKeyboardGif.Repository
) : ViewModel(), IContractNapoleonKeyboardGif.ViewModel {

    private val _downloadProgress = MutableLiveData<DownloadFileResult>()
    val downloadAttachmentProgress: LiveData<DownloadFileResult>
        get() = _downloadProgress

    /** [IContractNapoleonKeyboardGif.ViewModel] */
    //region Implementation IContractNapoleonKeyboardGif.ViewModel
    override fun downloadGif(mediaGif: Media) {
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
    //endregion
}