package com.vincent.videocompressor

import java.io.File

sealed class VideoCompressResult {

    object Start : VideoCompressResult()

    data class Success(val srcFile: File, val destFile: File) : VideoCompressResult()

    object Fail : VideoCompressResult()

    data class Progress(val progress: Float) : VideoCompressResult()

}