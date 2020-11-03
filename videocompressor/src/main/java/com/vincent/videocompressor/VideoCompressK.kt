package com.vincent.videocompressor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import java.io.File

object VideoCompressK {

    fun compressVideoHigh(
        srcFile: File,
        destFile: File,
        job: ProducerScope<*>
    ): Flow<VideoCompressResult> {
        return VideoControllerK.instance!!.convertVideo(
            srcFile,
            destFile,
            VideoController.COMPRESS_QUALITY_HIGH,
            job
        )
    }

    fun compressVideoMedium(
        srcFile: File,
        destFile: File,
        job: ProducerScope<*>
    ): Flow<VideoCompressResult> {
        return VideoControllerK.instance!!.convertVideo(
            srcFile,
            destFile,
            VideoController.COMPRESS_QUALITY_MEDIUM,
            job
        )
    }

    fun compressVideoLow(
        srcFile: File,
        destFile: File,
        job: ProducerScope<*>
    ): Flow<VideoCompressResult> {
        return VideoControllerK.instance!!.convertVideo(
            srcFile,
            destFile,
            VideoController.COMPRESS_QUALITY_LOW,
            job
        )
    }

    fun compressVideoCustom(
        srcFile: File,
        destFile: File,
        job: CoroutineScope
    ): Flow<VideoCompressResult> {
        return VideoControllerK.instance!!.convertVideo(
            srcFile,
            destFile,
            VideoController.COMPRESS_QUALITY_CUSTOM,
            job
        )
    }
}