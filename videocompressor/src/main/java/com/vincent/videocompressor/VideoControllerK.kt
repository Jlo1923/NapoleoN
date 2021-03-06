package com.vincent.videocompressor

import android.annotation.TargetApi
import android.media.MediaCodec
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import com.vincent.videocompressor.VideoController.COMPRESS_QUALITY_CUSTOM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class VideoControllerK {
    var path: String? = null
    private var videoConvertFirstWrite = true

    private fun didWriteData(last: Boolean, error: Boolean) {
        val firstWrite = videoConvertFirstWrite
        if (firstWrite) {
            videoConvertFirstWrite = false
        }
    }

    @TargetApi(16)
    @Throws(Exception::class)
    private fun readAndWriteTrack(
        extractor: MediaExtractor,
        mediaMuxer: MP4Builder?,
        info: MediaCodec.BufferInfo,
        start: Long,
        end: Long,
        file: File,
        isAudio: Boolean
    ): Long {
        val trackIndex = selectTrack(extractor, isAudio)
        if (trackIndex >= 0) {
            extractor.selectTrack(trackIndex)
            val trackFormat = extractor.getTrackFormat(trackIndex)
            val muxerTrackIndex = mediaMuxer!!.addTrack(trackFormat, isAudio)
            val maxBufferSize = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            var inputDone = false
            if (start > 0) {
                extractor.seekTo(start, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            } else {
                extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            }
            val buffer = ByteBuffer.allocateDirect(maxBufferSize)
            var startTime: Long = -1
            while (!inputDone) {
                var eof = false
                val index = extractor.sampleTrackIndex
                if (index == trackIndex) {
                    info.size = extractor.readSampleData(buffer, 0)
                    if (info.size < 0) {
                        info.size = 0
                        eof = true
                    } else {
                        info.presentationTimeUs = extractor.sampleTime
                        if (start > 0 && startTime == -1L) {
                            startTime = info.presentationTimeUs
                        }
                        if (end < 0 || info.presentationTimeUs < end) {
                            info.offset = 0
                            info.flags = extractor.sampleFlags
                            if (mediaMuxer.writeSampleData(
                                    muxerTrackIndex,
                                    buffer,
                                    info,
                                    isAudio
                                )
                            ) {
                                // didWriteData(messageObject, file, false, false);
                            }
                            extractor.advance()
                        } else {
                            eof = true
                        }
                    }
                } else if (index == -1) {
                    eof = true
                }
                if (eof) {
                    inputDone = true
                }
            }
            extractor.unselectTrack(trackIndex)
            return startTime
        }
        return -1
    }

    @TargetApi(16)
    private fun selectTrack(extractor: MediaExtractor, audio: Boolean): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (audio) {
                if (mime!!.startsWith("audio/")) {
                    return i
                }
            } else {
                if (mime!!.startsWith("video/")) {
                    return i
                }
            }
        }
        return -5
    }

    /**
     * Perform the actual video compression. Processes the frames and does the magic
     *
     * @param sourceFile      the source uri for the file as per
     * @param destinationFile the destination directory where compressed video is eventually saved
     * @return
     */
    @TargetApi(16)
    fun convertVideo(
        sourceFile: File,
        destinationFile: File,
        quality: Int,
        job: CoroutineScope
    ) = channelFlow {
        offer(VideoCompressResult.Start)
        path = sourceFile.absolutePath
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val width =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
        val rotation =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val duration =
            java.lang.Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000
        val startTime: Long = -1
        val endTime: Long = -1
        var rotationValue = Integer.valueOf(rotation)
        val originalWidth = Integer.valueOf(width)
        val originalHeight = Integer.valueOf(height)
        val originalBitrate = Integer.valueOf(bitrate)
        var resultWidth: Int
        var resultHeight: Int
        var resultBitrate: Int = originalBitrate
        when (quality) {
            COMPRESS_QUALITY_HIGH -> {
                resultWidth = originalWidth * 2 / 3
                resultHeight = originalHeight * 2 / 3
                resultBitrate = resultWidth * resultHeight * 30
            }
            COMPRESS_QUALITY_MEDIUM -> {
                resultWidth = originalWidth / 2
                resultHeight = originalHeight / 2
                resultBitrate = resultWidth * resultHeight * 10
            }
            COMPRESS_QUALITY_LOW -> {
                resultWidth = originalWidth / 2
                resultHeight = originalHeight / 2
                resultBitrate = resultWidth / 2 * (resultHeight / 2) * 10
            }
            COMPRESS_QUALITY_CUSTOM -> {
                println("*VideoController: Original(width: $originalWidth * height: $originalHeight bitrate: $originalBitrate)")
                if (originalWidth <= 1280 && originalHeight <= 720) {
                    resultWidth = originalWidth
                    resultHeight = originalHeight
                } else {
                    resultWidth = originalWidth * 2 / 3
                    resultHeight = originalHeight * 2 / 3
                }
                if (originalBitrate >= 3500) resultBitrate = resultWidth * resultHeight * 3

                println("*VideoController: Result(width: $resultWidth * height: $resultHeight bitrate: $resultBitrate)")
            }
            else -> {
                resultWidth = originalWidth / 2
                resultHeight = originalHeight / 2
                resultBitrate = resultWidth / 2 * (resultHeight / 2) * 7
            }
        }

        //TODO: quitar esta rotaci??n
        var rotateRender = 0
        val cacheFile = destinationFile
        if (Build.VERSION.SDK_INT < 18 && resultHeight > resultWidth && resultWidth != originalWidth && resultHeight != originalHeight) {
            val temp = resultHeight
            resultHeight = resultWidth
            resultWidth = temp
            rotationValue = 90
            rotateRender = 270
        } else if (Build.VERSION.SDK_INT > 20) {
            if (rotationValue == 90) {
                val temp = resultHeight
                resultHeight = resultWidth
                resultWidth = temp
                rotationValue = 0
                rotateRender = 270
            } else if (rotationValue == 180) {
                rotateRender = 180
                rotationValue = 0
            } else if (rotationValue == 270) {
                val temp = resultHeight
                resultHeight = resultWidth
                resultWidth = temp
                rotationValue = 0
                rotateRender = 90
            }
        }

        val inputFile = File(path ?: "default")
        if (!inputFile.canRead()) {
            didWriteData(true, error = true)
            offer(VideoCompressResult.Fail)
        }
        videoConvertFirstWrite = true
        var error = false
        var videoStartTime = startTime
        val time = System.currentTimeMillis()
        if (resultWidth != 0 && resultHeight != 0) {
            var mediaMixer: MP4Builder? = null
            var extractor: MediaExtractor? = null
            try {
                val info = MediaCodec.BufferInfo()
                val movie = Mp4Movie()
                movie.cacheFile = cacheFile
                movie.setRotation(rotationValue)
                movie.setSize(resultWidth, resultHeight)
                mediaMixer = MP4Builder().createMovie(movie)
                extractor = MediaExtractor()
                extractor.setDataSource(inputFile.toString())
                if (resultWidth != originalWidth || resultHeight != originalHeight) {
                    val videoIndex: Int = selectTrack(extractor, false)
                    if (videoIndex >= 0) {
                        var decoder: MediaCodec? = null
                        var encoder: MediaCodec? = null
                        var inputSurface: InputSurface? = null
                        var outputSurface: OutputSurface? = null
                        try {
                            var videoTime: Long = -1
                            var outputDone = false
                            var inputDone = false
                            var decoderDone = false
//                            val swapUV = 0
                            var videoTrackIndex = -5
                            val processorType = PROCESSOR_TYPE_OTHER
                            val manufacturer = Build.MANUFACTURER.toLowerCase(Locale.ROOT)
                            /*if (Build.VERSION.SDK_INT < 18) {
                                val codecInfo =
                                    selectCodec(MIME_TYPE)
                                colorFormat = selectColorFormat(
                                    codecInfo,
                                    MIME_TYPE
                                )
                                if (colorFormat == 0) {
                                    throw RuntimeException("no supported color format")
                                }
                                val codecName = codecInfo!!.name
                                if (codecName.contains("OMX.qcom.")) {
                                    processorType = PROCESSOR_TYPE_QCOM
                                    if (Build.VERSION.SDK_INT == 16) {
                                        if (manufacturer == "lge" || manufacturer == "nokia") {
                                            swapUV = 1
                                        }
                                    }
                                } else if (codecName.contains("OMX.Intel.")) {
                                    processorType = PROCESSOR_TYPE_INTEL
                                } else if (codecName == "OMX.MTK.VIDEO.ENCODER.AVC") {
                                    processorType = PROCESSOR_TYPE_MTK
                                } else if (codecName == "OMX.SEC.AVC.Encoder") {
                                    processorType = PROCESSOR_TYPE_SEC
                                    swapUV = 1
                                } else if (codecName == "OMX.TI.DUCATI1.VIDEO.H264E") {
                                    processorType = PROCESSOR_TYPE_TI
                                }
                                Log.e(
                                    "tmessages",
                                    "codec = " + codecInfo.name + " manufacturer = " + manufacturer + "device = " + Build.MODEL
                                )
                            } else {
                                colorFormat =
                                    CodecCapabilities.COLOR_FormatSurface
                            }*/

                            val colorFormat: Int = CodecCapabilities.COLOR_FormatSurface

                            Log.e("tmessages", "colorFormat = $colorFormat")
                            var resultHeightAligned = resultHeight
                            var padding: Int
                            var bufferSize = resultWidth * resultHeight * 3 / 2
                            if (processorType == PROCESSOR_TYPE_OTHER) {
                                if (resultHeight % 16 != 0) {
                                    resultHeightAligned += 16 - resultHeight % 16
                                    padding = resultWidth * (resultHeightAligned - resultHeight)
                                    bufferSize += padding * 5 / 4
                                }
                            } else if (processorType == PROCESSOR_TYPE_QCOM) {
                                if (manufacturer.toLowerCase() != "lge") {
                                    val uvoffset =
                                        resultWidth * resultHeight + 2047 and 2047.inv()
                                    padding = uvoffset - resultWidth * resultHeight
                                    bufferSize += padding
                                }
                            } else if (processorType == PROCESSOR_TYPE_TI) {
                                //resultHeightAligned = 368;
                                //bufferSize = resultWidth * resultHeightAligned * 3 / 2;
                                //resultHeightAligned += (16 - (resultHeight % 16));
                                //padding = resultWidth * (resultHeightAligned - resultHeight);
                                //bufferSize += padding * 5 / 4;
                            } else if (processorType == PROCESSOR_TYPE_MTK) {
                                if (manufacturer == "baidu") {
                                    resultHeightAligned += 16 - resultHeight % 16
                                    padding = resultWidth * (resultHeightAligned - resultHeight)
                                    bufferSize += padding * 5 / 4
                                }
                            }
                            extractor.selectTrack(videoIndex)
                            extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                            val inputFormat = extractor.getTrackFormat(videoIndex)
                            val outputFormat = MediaFormat.createVideoFormat(
                                MIME_TYPE,
                                resultWidth,
                                resultHeight
                            )
                            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
                            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, resultBitrate)
                            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
                            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
                            /*if (Build.VERSION.SDK_INT < 18) {
                                outputFormat.setInteger("stride", resultWidth + 32)
                                outputFormat.setInteger("slice-height", resultHeight)
                            }*/
                            encoder =
                                MediaCodec.createEncoderByType(MIME_TYPE)
                            encoder.configure(
                                outputFormat,
                                null,
                                null,
                                MediaCodec.CONFIGURE_FLAG_ENCODE
                            )
                            inputSurface = InputSurface(encoder.createInputSurface())
                            inputSurface.makeCurrent()
                            encoder.start()
                            decoder =
                                MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME)
                                    .toString())
                            outputSurface = OutputSurface()
                            decoder.configure(inputFormat, outputSurface.surface, null, 0)
                            decoder.start()
                            val TIME_OUT_USEC = 2500

                            /*val decoderInputBuffers: Array<ByteBuffer?>? = null
                            var encoderOutputBuffers: Array<ByteBuffer?>? = null
                            val encoderInputBuffers: Array<ByteBuffer>? = null
                            if (Build.VERSION.SDK_INT < 21) {
                                decoderInputBuffers = decoder.inputBuffers
                                encoderOutputBuffers = encoder.outputBuffers
                                if (Build.VERSION.SDK_INT < 18) {
                                    encoderInputBuffers = encoder.inputBuffers
                                }
                            }*/

                            while (!outputDone) {
                                if (job.isActive) {
                                    if (!inputDone) {
                                        var eof = false
                                        val index = extractor.sampleTrackIndex
                                        if (index == videoIndex) {
                                            val inputBufIndex =
                                                decoder.dequeueInputBuffer(TIME_OUT_USEC.toLong())
                                            if (inputBufIndex >= 0) {
                                                val inputBuf: ByteBuffer? =
                                                    decoder.getInputBuffer(inputBufIndex)
                                                val chunkSize =
                                                    extractor.readSampleData(inputBuf!!, 0)
                                                if (chunkSize < 0) {
                                                    decoder.queueInputBuffer(
                                                        inputBufIndex,
                                                        0,
                                                        0,
                                                        0L,
                                                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                                    )
                                                    inputDone = true
                                                } else {
                                                    decoder.queueInputBuffer(
                                                        inputBufIndex,
                                                        0,
                                                        chunkSize,
                                                        extractor.sampleTime,
                                                        0
                                                    )
                                                    extractor.advance()
                                                }
                                            }
                                        } else if (index == -1) {
                                            eof = true
                                        }
                                        if (eof) {
                                            val inputBufIndex =
                                                decoder.dequeueInputBuffer(TIME_OUT_USEC.toLong())
                                            if (inputBufIndex >= 0) {
                                                decoder.queueInputBuffer(
                                                    inputBufIndex,
                                                    0,
                                                    0,
                                                    0L,
                                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                                )
                                                inputDone = true
                                            }
                                        }
                                    }
                                    var decoderOutputAvailable = !decoderDone
                                    var encoderOutputAvailable = true
                                    while (decoderOutputAvailable || encoderOutputAvailable) {
                                        if (job.isActive) {
                                            val encoderStatus =
                                                encoder.dequeueOutputBuffer(
                                                    info,
                                                    TIME_OUT_USEC.toLong()
                                                )
                                            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                                encoderOutputAvailable = false
                                            } /*else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                                if (Build.VERSION.SDK_INT < 21) {
                                                    encoderOutputBuffers = encoder.outputBuffers
                                                }
                                            }*/ else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                                val newFormat = encoder.outputFormat
                                                if (videoTrackIndex == -5) {
                                                    videoTrackIndex =
                                                        mediaMixer.addTrack(newFormat, false)
                                                }
                                            } else if (encoderStatus < 0) {
                                                throw RuntimeException("unexpected result from encoder.dequeueOutputBuffer: $encoderStatus")
                                            } else {
                                                val encodedData: ByteBuffer? =
                                                    encoder.getOutputBuffer(encoderStatus)
                                                if (encodedData == null) {
                                                    throw RuntimeException("encoderOutputBuffer $encoderStatus was null")
                                                }
                                                if (info.size > 1) {
                                                    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG == 0) {
                                                        if (mediaMixer.writeSampleData(
                                                                videoTrackIndex,
                                                                encodedData,
                                                                info,
                                                                false
                                                            )
                                                        ) {
                                                            didWriteData(false, error = false)
                                                        }
                                                    } else if (videoTrackIndex == -5) {
                                                        val csd =
                                                            ByteArray(info.size)
                                                        encodedData.limit(info.offset + info.size)
                                                        encodedData.position(info.offset)
                                                        encodedData[csd]
                                                        var sps: ByteBuffer? = null
                                                        var pps: ByteBuffer? = null
                                                        for (a in info.size - 1 downTo 0) {
                                                            if (a > 3) {
                                                                if (csd[a] == 1.toByte() && csd[a - 1] == 0.toByte() && csd[a - 2] == 0.toByte() && csd[a - 3] == 0.toByte()) {
                                                                    sps =
                                                                        ByteBuffer.allocate(a - 3)
                                                                    pps =
                                                                        ByteBuffer.allocate(info.size - (a - 3))
                                                                    sps.put(csd, 0, a - 3)
                                                                        .position(0)
                                                                    pps.put(
                                                                        csd,
                                                                        a - 3,
                                                                        info.size - (a - 3)
                                                                    )
                                                                        .position(0)
                                                                    break
                                                                }
                                                            } else {
                                                                break
                                                            }
                                                        }
                                                        val newFormat =
                                                            MediaFormat.createVideoFormat(
                                                                MIME_TYPE,
                                                                resultWidth,
                                                                resultHeight
                                                            )
                                                        if (sps != null && pps != null) {
                                                            newFormat.setByteBuffer(
                                                                "csd-0",
                                                                sps
                                                            )
                                                            newFormat.setByteBuffer(
                                                                "csd-1",
                                                                pps
                                                            )
                                                        }
                                                        videoTrackIndex =
                                                            mediaMixer.addTrack(
                                                                newFormat,
                                                                false
                                                            )
                                                    }
                                                }
                                                outputDone =
                                                    info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
                                                encoder.releaseOutputBuffer(
                                                    encoderStatus,
                                                    false
                                                )
                                            }
                                            if (encoderStatus != MediaCodec.INFO_TRY_AGAIN_LATER) {
                                                continue
                                            }
                                            if (!decoderDone) {
                                                val decoderStatus =
                                                    decoder.dequeueOutputBuffer(
                                                        info,
                                                        TIME_OUT_USEC.toLong()
                                                    )
                                                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                                                    decoderOutputAvailable = false
                                                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                                                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                                    val newFormat = decoder.outputFormat
                                                    Log.e(
                                                        "tmessages",
                                                        "newFormat = $newFormat"
                                                    )
                                                } else if (decoderStatus < 0) {
                                                    throw RuntimeException("unexpected result from decoder.dequeueOutputBuffer: $decoderStatus")
                                                } else {
                                                    var doRender: Boolean
                                                    doRender = info.size != 0
                                                    if (endTime > 0 && info.presentationTimeUs >= endTime) {
                                                        inputDone = true
                                                        decoderDone = true
                                                        doRender = false
                                                        info.flags =
                                                            info.flags or MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                                    }
                                                    if (startTime > 0 && videoTime == -1L) {
                                                        if (info.presentationTimeUs < startTime) {
                                                            doRender = false
                                                            Log.e(
                                                                "tmessages",
                                                                "drop frame startTime = " + startTime + " present time = " + info.presentationTimeUs
                                                            )
                                                        } else {
                                                            videoTime = info.presentationTimeUs
                                                        }
                                                    }
                                                    decoder.releaseOutputBuffer(
                                                        decoderStatus,
                                                        doRender
                                                    )
                                                    if (doRender) {
                                                        var errorWait = false
                                                        try {
                                                            outputSurface.awaitNewImage()
                                                        } catch (e: Exception) {
                                                            errorWait = true
                                                            Log.e("tmessages", e.message.toString())
                                                        }
                                                        if (!errorWait) {
                                                            outputSurface.drawImage(false)
                                                            inputSurface!!.setPresentationTime(
                                                                info.presentationTimeUs * 1000
                                                            )
                                                            Log.d("tmessages", "$job")
                                                            offer(
                                                                VideoCompressResult.Progress(
                                                                    info.presentationTimeUs.toFloat() / duration.toFloat() * 100
                                                                )
                                                            )
                                                            inputSurface.swapBuffers()
                                                        }
                                                    }
                                                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                                        decoderOutputAvailable = false
                                                        Log.e(
                                                            "tmessages",
                                                            "decoder stream end"
                                                        )
                                                        encoder.signalEndOfInputStream()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (videoTime != -1L) {
                                videoStartTime = videoTime
                            }
                        } catch (e: Exception) {
                            Log.e("tmessages", e.message.toString())
                            error = true
                        }
                        extractor.unselectTrack(videoIndex)
                        outputSurface?.release()
                        inputSurface?.release()
                        if (decoder != null) {
                            decoder.stop()
                            decoder.release()
                        }
                        if (encoder != null) {
                            encoder.stop()
                            encoder.release()
                        }
                    }
                } else {
                    val videoTime = readAndWriteTrack(
                        extractor,
                        mediaMixer,
                        info,
                        startTime,
                        endTime,
                        cacheFile,
                        false
                    )
                    if (videoTime != -1L) {
                        videoStartTime = videoTime
                    }
                }
                if (!error) {
                    readAndWriteTrack(
                        extractor,
                        mediaMixer,
                        info,
                        videoStartTime,
                        endTime,
                        cacheFile,
                        true
                    )
                }
            } catch (e: Exception) {
                error = true
                Log.e("tmessages", e.message.toString())
                offer(VideoCompressResult.Fail)
            } finally {
                extractor?.release()
                if (mediaMixer != null) {
                    try {
                        mediaMixer.finishMovie(false)
                    } catch (e: Exception) {
                        Log.e("tmessages", e.message.toString())
                    }
                }
                Log.e(
                    "tmessages",
                    "time = " + (System.currentTimeMillis() - time)
                )
            }
        } else {
            didWriteData(true, true)
            offer(VideoCompressResult.Fail)
        }
        didWriteData(true, error)
        cachedFile = cacheFile

        /* File fdelete = inputFile;
        if (fdelete.exists()) {
            if (fdelete.delete()) {
               Log.e("file Deleted :" ,inputFile.getPath());
            } else {
                Log.e("file not Deleted :" , inputFile.getPath());
            }
        }*/

        //inputFile.delete();
        /*Log.e("ViratPath", path + "")
        Log.e("ViratPath", cacheFile.path + "")
        Log.e("ViratPath", inputFile.path + "")*/


        /* Log.e("ViratPath",path+"");
        File replacedFile = new File(path);

        FileOutputStream fos = null;
        InputStream inputStream = null;
        try {
            fos = new FileOutputStream(replacedFile);
             inputStream = new FileInputStream(cacheFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            inputStream.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        //    cacheFile.delete();

        /* try {
           // copyFile(cacheFile,inputFile);
            //inputFile.delete();
            FileUtils.copyFile(cacheFile,inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // cacheFile.delete();
        // inputFile.delete();
        offer(VideoCompressResult.Success(sourceFile, destinationFile))
    }

    companion object {
        const val COMPRESS_QUALITY_HIGH = 1
        const val COMPRESS_QUALITY_MEDIUM = 2
        const val COMPRESS_QUALITY_LOW = 3
        var cachedFile: File? = null
        const val MIME_TYPE = "video/avc"
        private const val PROCESSOR_TYPE_OTHER = 0
        private const val PROCESSOR_TYPE_QCOM = 1
        private const val PROCESSOR_TYPE_INTEL = 2
        private const val PROCESSOR_TYPE_MTK = 3
        private const val PROCESSOR_TYPE_SEC = 4
        private const val PROCESSOR_TYPE_TI = 5

        @Volatile
        var instance: VideoControllerK? = null
            get() {
                var localInstance = field
                if (localInstance == null) {
                    synchronized(VideoControllerK::class.java) {
                        localInstance = field
                        if (localInstance == null) {
                            localInstance = VideoControllerK()
                            field = localInstance
                        }
                    }
                }
                return localInstance
            }
            private set

        /*@SuppressLint("NewApi")
        fun selectColorFormat(
            codecInfo: MediaCodecInfo?,
            mimeType: String?
        ): Int {
            val capabilities = codecInfo!!.getCapabilitiesForType(mimeType)
            var lastColorFormat = 0
            for (i in capabilities.colorFormats.indices) {
                val colorFormat = capabilities.colorFormats[i]
                if (isRecognizedFormat(colorFormat)) {
                    lastColorFormat = colorFormat
                    if (!(codecInfo.name == "OMX.SEC.AVC.Encoder" && colorFormat == 19)) {
                        return colorFormat
                    }
                }
            }
            return lastColorFormat
        }*/

        /*private fun isRecognizedFormat(colorFormat: Int): Boolean {
            return when (colorFormat) {
                CodecCapabilities.COLOR_FormatYUV420Planar, CodecCapabilities.COLOR_FormatYUV420PackedPlanar, CodecCapabilities.COLOR_FormatYUV420SemiPlanar, CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar, CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar -> true
                else -> false
            }
        }*/

        /*external fun convertVideoFrame(
            src: ByteBuffer?,
            dest: ByteBuffer?,
            destFormat: Int,
            width: Int,
            height: Int,
            padding: Int,
            swap: Int
        ): Int*/

        /*fun selectCodec(mimeType: String?): MediaCodecInfo? {
            val numCodecs = MediaCodecList.getCodecCount()
            var lastCodecInfo: MediaCodecInfo? = null
            for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) {
                    continue
                }
                val types = codecInfo.supportedTypes
                for (type in types) {
                    if (type.equals(mimeType, ignoreCase = true)) {
                        lastCodecInfo = codecInfo
                        if (lastCodecInfo.name != "OMX.SEC.avc.enc") {
                            return lastCodecInfo
                        } else if (lastCodecInfo.name == "OMX.SEC.AVC.Encoder") {
                            return lastCodecInfo
                        }
                    }
                }
            }
            return lastCodecInfo
        }*/

        /*@Throws(IOException::class)
        fun copyFile(src: File?, dst: File?) {
            val inChannel =
                FileInputStream(src).channel
            val outChannel =
                FileOutputStream(dst).channel
            try {
                inChannel!!.transferTo(1, inChannel.size(), outChannel)
            } finally {
                inChannel?.close()
                outChannel?.close()
            }
        }*/
    }
}