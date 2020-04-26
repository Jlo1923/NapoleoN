package com.naposystems.pepito.webService

import com.naposystems.pepito.entity.message.attachments.Attachment
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.ByteArrayInputStream

class ProgressRequestBody(
    private val attachment: Attachment,
    private val bytes: ByteArray,
    private val mediaType: MediaType,
    private val listener: Listener
) : RequestBody() {

    private val mLength = bytes.size.toLong()

    interface Listener {
        fun onRequestProgress(bytesWritten: Int, contentLength: Long, progress: Int)
    }

    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long = mLength

    override fun writeTo(sink: BufferedSink) {
        ByteArrayInputStream(bytes).use { inputStream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var uploaded = 0
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                listener.onRequestProgress(
                    uploaded,
                    mLength,
                    (100f * uploaded / mLength).toInt()
                )
            }
        }
    }
}