package com.naposystems.pepito.ui.napoleonKeyboardGif

import com.giphy.sdk.core.models.Media
import com.naposystems.pepito.utility.DownloadFileResult
import kotlinx.coroutines.flow.Flow

interface IContractNapoleonKeyboardGif {

    interface ViewModel {
        fun downloadGif(mediaGif: Media)
    }

    interface Repository {
        suspend fun downloadGif(url: String): Flow<DownloadFileResult>
    }
}