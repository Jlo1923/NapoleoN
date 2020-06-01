package com.naposystems.pepito.webService

import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.UploadResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.channelFlow
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.net.SocketException

class ProgressRequestBody(
    private val message: Message,
    private val attachment: Attachment,
    private val channel: ProducerScope<UploadResult>,
    private val job: Job,
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

                        channel.offer(
                            UploadResult.Progress(
                                attachment,
                                (100f * uploaded / mLength).toLong(),
                                job
                            )
                        )

                        /*listener.onRequestProgress(
                            uploaded,
                            mLength,
                            (100f * uploaded / mLength).toInt()
                        )*/
                    } else {
                        Timber.e("Job no active")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            listener.onRequestCancel()
        }
    }
}