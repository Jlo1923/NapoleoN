package com.naposystems.pepito.webService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.net.SocketException

class ProgressRequestBody(
    private val job: CoroutineScope,
    private val bytes: ByteArray,
    private val mediaType: MediaType,
    private val listener: Listener
) : RequestBody() {

    private val mLength = bytes.size.toLong()

    interface Listener {
        fun onRequestProgress(bytesWritten: Int, contentLength: Long, progress: Int)
        fun onRequestCancel()
    }

    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long = mLength

    override fun writeTo(sink: BufferedSink) {
        try {
            ByteArrayInputStream(bytes).use { inputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded = 0
                var read: Int

                while (inputStream.read(buffer).also { read = it } != -1) {
                    if (job.isActive) {
                        sink.write(buffer, 0, read)
                        uploaded += read
                        listener.onRequestProgress(
                            uploaded,
                            mLength,
                            (100f * uploaded / mLength).toInt()
                        )
                    } else {
                        Timber.d("Job no active")
                        listener.onRequestCancel()
                        break
                    }
                }
            }
        } catch (e: SocketException) {
            Timber.d("Job cancel")
            listener.onRequestCancel()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}