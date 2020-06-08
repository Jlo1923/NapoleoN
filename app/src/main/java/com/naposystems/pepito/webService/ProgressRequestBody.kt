package com.naposystems.pepito.webService

import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.UploadResult
import kotlinx.coroutines.channels.ProducerScope
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import timber.log.Timber
import java.io.ByteArrayInputStream

class ProgressRequestBody(
    private val attachment: Attachment,
    private val channel: ProducerScope<UploadResult>,
    private val bytes: ByteArray,
    private val mediaType: MediaType
) : RequestBody() {

    private val mLength = bytes.size.toLong()

    interface Listener {
        fun onRequestCancel()
    }

    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long = mLength

    override fun writeTo(sink: BufferedSink) {
        Timber.d("writeTo")
        try {
            ByteArrayInputStream(bytes).use { inputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var uploaded = 0
                var read: Int

                while (inputStream.read(buffer).also { read = it } != -1) {
                    sink.write(buffer, 0, read)
                    uploaded += read

                    channel.offer(
                        UploadResult.Progress(
                            attachment,
                            (100f * uploaded / mLength).toLong(),
                            channel
                        )
                    )
                }
            }
        } catch (e: Exception) {
            channel.close()
            Timber.e(e)
        }
    }
}