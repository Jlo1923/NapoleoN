package com.naposystems.napoleonchat.webService

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import timber.log.Timber
import java.io.ByteArrayInputStream

class ProgressRequestBody(
    private val bytes: ByteArray,
    private val mediaType: MediaType,
    private val progress: (Float) -> Unit
) : RequestBody() {

    private val mLength = bytes.size.toLong()
    private var writeToCall = 0

    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long = mLength

    override fun writeTo(sink: BufferedSink) {
        Timber.d("UploadServiceRepository writeTo")
        writeToCall++
        try {
            ByteArrayInputStream(bytes).use { inputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded = 0
                var read: Int

                while (inputStream.read(buffer).also { read = it } != -1) {
                    sink.write(buffer, 0, read)
                    uploaded += read
                    if (writeToCall == 2) {
                        progress(100f * uploaded / mLength)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}