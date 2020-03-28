package com.naposystems.pepito.ui.napoleonKeyboardGif

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.giphy.sdk.core.models.Media
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class NapoleonKeyboardGifViewModel @Inject constructor(
    private val repository: IContractNapoleonKeyboardGif.Repository
) : ViewModel(), IContractNapoleonKeyboardGif.ViewModel {

    private val _downloadProgress = MutableLiveData<Float>()
    val downloadProgress: LiveData<Float>
        get() = _downloadProgress

    private val _gifFileName = MutableLiveData<String>()
    val gifFileName: LiveData<String>
        get() = _gifFileName

    private val _errorDownloading = MutableLiveData<String>()
    val errorDownloading: LiveData<String>
        get() = _errorDownloading

    /** [IContractNapoleonKeyboardGif.ViewModel] */
    //region Implementation IContractNapoleonKeyboardGif.ViewModel
    override fun downloadGif(mediaGif: Media) {
        viewModelScope.launch {
            try {
                mediaGif.images.original?.gifUrl?.let { gifUrl ->
                    _gifFileName.value = repository.downloadGif(gifUrl, _downloadProgress)
                }
            } catch (e: Exception) {
                Timber.e(e)
                _errorDownloading.value = "Falló la descarga|!!"
            }
        }
    }
    //endregion
}