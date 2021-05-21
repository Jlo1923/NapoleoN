package com.naposystems.napoleonchat.repository.napoleonKeyboardGif

import com.naposystems.napoleonchat.utility.DownloadFileResult
import kotlinx.coroutines.flow.Flow

interface NapoleonKeyboardGifRepository {
    suspend fun downloadGif(url: String): Flow<DownloadFileResult>
}