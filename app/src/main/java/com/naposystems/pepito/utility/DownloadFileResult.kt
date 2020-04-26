package com.naposystems.pepito.utility

sealed class DownloadFileResult {

    data class Success(val fileName: String) : DownloadFileResult()

    data class Error(
        val message: String,
        val cause: Exception? = null
    ) : DownloadFileResult()

    data class Progress(val progress: Long) : DownloadFileResult()
}