package com.naposystems.pepito.ui.conversationCamera

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationCameraFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

@SuppressLint("RestrictedApi")
class ConversationCameraFragment : Fragment() {

    private lateinit var binding: ConversationCameraFragmentBinding
    private lateinit var analysisExecutor: Executor
    private lateinit var mainExecutor: Executor

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoFile: File
    private lateinit var path: File
    private lateinit var videoFile: File
    private lateinit var fileName: String
    private var recordingTime: Long = 0
    private var mStartToRecordRunnable: Runnable = Runnable { startRecording() }
    private lateinit var mRecordingTimeRunnable: Runnable

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var torchEnable = false
    private var isRecording: Boolean = false
    private val mHandler: Handler by lazy {
        Handler()
    }
    private val args: ConversationCameraFragmentArgs by navArgs()

    companion object {

        private const val PHOTO_EXTENSION = ".jpg"
        private const val VIDEO_EXTENSION = ".mp4"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainExecutor = ContextCompat.getMainExecutor(requireContext())
        analysisExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.conversation_camera_fragment,
            container,
            false
        )

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.viewFinder.post {
            startCamera()
        }

        binding.imageButtonSwitchCamera.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            startCamera()
        }

        binding.imageButtonFlash.setOnClickListener {
            torchEnable = !torchEnable
            imageCapture!!.flashMode =
                if (torchEnable) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            binding.imageButtonFlash.setImageDrawable(
                context!!.resources.getDrawable(
                    if (torchEnable) R.drawable.ic_flash_on_black else R.drawable.ic_flash_off_black,
                    context!!.theme
                )
            )
        }

        binding.imageButtonCamera.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mHandler.postDelayed(mStartToRecordRunnable, 500)
            } else if (event.action == MotionEvent.ACTION_UP) {
                mHandler.removeCallbacks(mStartToRecordRunnable)
                if (!isRecording) {
                    takePhoto()
                }
                stopRecording()
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        videoCapture?.stopRecording()
        videoCapture?.clear()
        if (::mRecordingTimeRunnable.isInitialized) {
            mHandler.removeCallbacks(mRecordingTimeRunnable)
        }
        mHandler.removeCallbacks(mStartToRecordRunnable)
    }

    private fun startCamera() {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        Timber.d("Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Timber.d("Preview aspect ratio: $screenAspectRatio")

        val rotation = binding.viewFinder.display.rotation

        // Bind the CameraProvider to the LifeCycleOwner
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation
                .setTargetRotation(rotation)
                .build()

            // Default PreviewSurfaceProvider
            preview!!.setSurfaceProvider(binding.viewFinder.previewSurfaceProvider)

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(720, 1280))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(rotation)
                .build()

            // VideoCapture
            videoCapture = VideoCaptureConfig.Builder()
                .setTargetRotation(rotation)
                .build()

            // ImageAnalysis
            imageAnalyzer = ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(analysisExecutor, LuminosityAnalyzer { luma ->
                        // Values returned from our analyzer are passed to the attached listener
                        // We log image analysis results here - you should do something useful instead!
                        //Timber.d("Average luminosity: $luma")
                    })
                }

            // Must unbind the use-cases before rebinding them.
            cameraProvider.unbindAll()

            try {
                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
            } catch (exc: Exception) {
                Timber.e("Use case binding failed, exc: $exc")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        photoFile = createFile(PHOTO_EXTENSION)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .setMetadata(ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }).build()

        imageCapture!!.takePicture(
            outputFileOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    val attachment = Attachment(
                        id = 0,
                        messageId = 0,
                        webId = "",
                        messageWebId = "",
                        type = Constants.AttachmentType.IMAGE.type,
                        body = "",
                        uri = photoFile.name,
                        origin = Constants.AttachmentOrigin.CAMERA.origin,
                        thumbnailUri = "",
                        status = Constants.AttachmentStatus.SENDING.status
                    )

                    findNavController().navigate(
                        ConversationCameraFragmentDirections.actionConversationCameraFragmentToAttachmentPreviewFragment(
                            attachment,
                            0,
                            args.quote
                        )
                    )

                    Timber.d("Photo capture succeeded: ${outputFileResults.savedUri}")
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.e("Photo capture failed: $exception")
                }
            })
    }

    private fun startRecording() {
        isRecording = true
        binding.imageButtonCamera.setBackgroundResource(R.drawable.bg_button_recoding)
        binding.imageButtonCamera.setImageResource(android.R.color.transparent)
        videoFile = createFile(VIDEO_EXTENSION)
        videoCapture?.startRecording(
            videoFile,
            mainExecutor,
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(file: File) {

                    val attachment = Attachment(
                        id = 0,
                        messageId = 0,
                        webId = "",
                        messageWebId = "",
                        type = Constants.AttachmentType.VIDEO.type,
                        body = "",
                        uri = videoFile.name,
                        origin = Constants.AttachmentOrigin.CAMERA.origin,
                        thumbnailUri = "",
                        status = Constants.AttachmentStatus.SENDING.status
                    )

                    videoCapture?.clear()
                    isRecording = false
                    mHandler.removeCallbacks(mRecordingTimeRunnable)
                    hideRecordingTime()

                    findNavController().navigate(
                        ConversationCameraFragmentDirections.actionConversationCameraFragmentToAttachmentPreviewFragment(
                            attachment,
                            0,
                            args.quote
                        )
                    )
                }

                override fun onError(
                    videoCaptureError: Int,
                    message: String,
                    cause: Throwable?
                ) {
                    mHandler.removeCallbacks(mRecordingTimeRunnable)
                    hideRecordingTime()
                    Timber.e("Video Error: $message")
                }
            })

        mRecordingTimeRunnable = Runnable {
            binding.lottieRecording.visibility = View.VISIBLE
            binding.textViewRecordingTime.apply {
                visibility = View.VISIBLE
                text = Utils.getDuration(recordingTime, showHours = false)
            }

            val oneSecond = TimeUnit.SECONDS.toMillis(1)
            recordingTime += oneSecond
            mHandler.postDelayed(mRecordingTimeRunnable, oneSecond)
        }
        mHandler.postDelayed(mRecordingTimeRunnable, 0)
    }

    private fun hideRecordingTime() {
        binding.lottieRecording.visibility = View.GONE
        binding.textViewRecordingTime.visibility = View.GONE
    }

    private fun stopRecording() {
        binding.imageButtonCamera.setBackgroundColor(
            resources.getColor(
                android.R.color.transparent,
                context!!.theme
            )
        )
        videoCapture?.stopRecording()
        isRecording = false
        Timber.i("Video File stopped")
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun createFile(extension: String): File {
        val timeStamp: String = System.currentTimeMillis().toString()

        val subFolder = when (extension) {
            PHOTO_EXTENSION -> Constants.NapoleonCacheDirectories.IMAGES.folder
            VIDEO_EXTENSION -> Constants.NapoleonCacheDirectories.VIDEOS.folder
            else -> ""
        }

        fileName = "${timeStamp}$extension"
        path = File(context!!.cacheDir!!, subFolder)
        if (!path.exists())
            path.mkdirs()

        // Create an image file name
        return File(path, fileName)
    }

    /**
     * Our custom image analysis class.
     *
     * <p>All we need to do is override the function `analyze` with our desired operations. Here,
     * we compute the average luminosity of the image by looking at the Y plane of the YUV frame.
     */
    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

}
