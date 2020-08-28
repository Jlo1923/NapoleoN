package com.naposystems.napoleonchat.webService

import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.UploadResult
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.isActive
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

                    if (channel.isActive) {
                        channel.offer(
                            UploadResult.Progress(
                                attachment,
                                (100f * uploaded / mLength),
                                channel
                            )
                        )
                    }

                    Timber.d("*Test1: ${uploaded * 100} / $mLength = ${uploaded * 100f / mLength}")
                }
            }
        } catch (e: Exception) {
            channel.close()
            Timber.e(e)
        }
    }
}