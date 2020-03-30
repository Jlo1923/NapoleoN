package com.naposystems.pepito.ui.napoleonKeyboardGif

import androidx.lifecycle.MutableLiveData
import com.giphy.sdk.core.models.Media

interface IContractNapoleonKeyboardGif {

    interface ViewModel {
        fun downloadGif(mediaGif: Media)
    }

    interface Repository {
        suspend fun downloadGif(url: String, progressLiveData: MutableLiveData<Float>): String
    }
}