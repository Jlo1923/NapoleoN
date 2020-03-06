package com.naposystems.pepito.ui.conversationCamera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.*
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationCameraNewFragmentBinding
import com.naposystems.pepito.utility.OrientationLiveData
import com.naposystems.pepito.utility.computeExifOrientation
import com.naposystems.pepito.utility.getPreviewOutputSize
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ConversationCameraNewFragment : Fragment() {

    companion object {

        private const val CAMERA_FRONT = "1"
        private const val CAMERA_BACK = "0"

        /** Maximum number of images that will be held in the reader's buffer */
        private const val IMAGE_BUFFER_SIZE: Int = 3

        /** Maximum time allowed to wait for the result of an image capture */
        private const val IMAGE_CAPTURE_TIMEOUT_MILLIS: Long = 5000

        /** Helper data class used to hold capture metadata with their associated image */
        data class CombinedCaptureResult(
            val image: Image,
            val metadata: CaptureResult,
            val orientation: Int,
            val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }

        fun newInstance() = ConversationCameraNewFragment()
    }

    private val viewModel: ConversationCameraNewViewModel by viewModels()
    private val shareViewModel: ConversationShareViewModel by activityViewModels()
    private val args: ConversationCameraNewFragmentArgs by navArgs()
    private lateinit var binding: ConversationCameraNewFragmentBinding
    private lateinit var photoFile: File

    private val subFolder by lazy {
        "conversations/${args.userId}_${args.contactId}"
    }

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private var cameraId: String = CAMERA_BACK
    private var torchEnable = false

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraId)
    }

    /** Readers used as buffers for camera still shots */
    private lateinit var imageReader: ImageReader

    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)

    /** [HandlerThread] where all buffer reading operations run */
    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

    /** [Handler] corresponding to [imageReaderThread] */
    private val imageReaderHandler = Handler(imageReaderThread.looper)

    /** The [CameraDevice] that will be opened in this fragment */
    private lateinit var camera: CameraDevice

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private lateinit var session: CameraCaptureSession

    /**Live data listener for changes in the device orientation relative to the camera */
    private lateinit var relativeOrientation: OrientationLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.viewSwitcher.currentView.id == binding.containerPreview.id) {
                backToCamera()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.conversation_camera_new_fragment,
            container,
            false
        )

        binding.imageButtonBack.setOnClickListener {
            backToCamera()
        }

        binding.imageButtonSwitchCamera.setOnClickListener {

            it.isEnabled = false

            if (cameraId == CAMERA_FRONT) {
                cameraId = CAMERA_BACK
                binding.imageButtonFlash.visibility = View.VISIBLE

            } else if (cameraId == CAMERA_BACK) {
                cameraId = CAMERA_FRONT
                binding.imageButtonFlash.visibility = View.INVISIBLE
            }

            camera.close()

            it.post {
                initializeCamera()
                it.isEnabled = true
            }
        }

        binding.imageButtonFlash.setOnClickListener {
            it.isEnabled = false

            torchEnable = !torchEnable

            val captureRequest = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            ).apply {

                if (torchEnable) {
                    set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                    )
                    set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF)
                } else {
                    set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                }

                addTarget(binding.surfaceView.holder.surface)
            }

            // This will keep sending the capture request as frequently as possible until the
            // session is torn down or session.stopRepeating() is called
            session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)

            binding.imageButtonFlash.setImageDrawable(
                context!!.resources.getDrawable(
                    if (torchEnable) R.drawable.ic_flash_on_black else R.drawable.ic_flash_off_black,
                    context!!.theme
                )
            )

            it.post {
                it.isEnabled = true
            }
        }

        binding.inputPanel.getFloatingActionButton().setOnClickListener {
            shareViewModel.setMessage(binding.inputPanel.getEditTex().text.toString())
            shareViewModel.setCameraSendClicked()
            shareViewModel.resetCameraSendClicked()
            shareViewModel.resetMessage()
            this.findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) = Unit

            override fun surfaceDestroyed(holder: SurfaceHolder?) = Unit

            override fun surfaceCreated(holder: SurfaceHolder?) {
                // Selects appropriate preview size and configures view finder
                val previewSize = getPreviewOutputSize(
                    binding.surfaceView.display, characteristics, SurfaceHolder::class.java
                )
                Timber.d("View finder size: ${binding.surfaceView.width} x ${binding.surfaceView.height}")
                Timber.d("Selected preview size: $previewSize")
                binding.surfaceView.holder.setFixedSize(previewSize.width, previewSize.height)

                // To ensure that size is set, initialize camera in the view's thread
                binding.surfaceView.post { initializeCamera() }
            }
        })

        // Used to rotate the output media to match device orientation
        relativeOrientation = OrientationLiveData(requireContext(), characteristics).apply {
            observe(viewLifecycleOwner, Observer { orientation ->
                Timber.d("Orientation changed: $orientation")
            })
        }
    }

    private fun backToCamera() {
        if (binding.viewSwitcher.currentView.id == binding.containerPreview.id) {
            if (photoFile.exists()) {
                photoFile.delete()
            }
            shareViewModel.setMediaBase64("")
            binding.viewSwitcher.showNext()
        }
    }

    /**
     * Begin all camera operations in a coroutine in the main thread. This function:
     * - Opens the camera
     * - Configures the camera session
     * - Starts the preview by dispatching a repeating capture request
     * - Sets up the still image capture listeners
     */
    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        // Open the selected camera
        camera = openCamera(cameraManager, cameraId, cameraHandler)

        // Initialize an image reader which will be used to capture still photos
        val outPutsSizes = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!
            .getOutputSizes(ImageFormat.JPEG).maxBy { 1920 * 1080 }

        val size = Size(1280, 720)
        imageReader = ImageReader.newInstance(
            size.width, size.height, ImageFormat.JPEG, IMAGE_BUFFER_SIZE
        )

        // Creates list of Surfaces where the camera will output frames
        val targets = listOf(binding.surfaceView.holder.surface, imageReader.surface)

        // Start a capture session using our open camera and list of Surfaces where frames will go
        session = createCaptureSession(camera, targets, cameraHandler)

        val captureRequest = camera.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        ).apply {
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
//            set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
            addTarget(binding.surfaceView.holder.surface)
        }

        // This will keep sending the capture request as frequently as possible until the
        // session is torn down or session.stopRepeating() is called
        session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)

        binding.imageButtonCamera.setOnClickListener {

            // Disable click listener to prevent multiple requests simultaneously in flight
            it.isEnabled = false

            // Perform I/O heavy operations in a different scope
            lifecycleScope.launch(Dispatchers.IO) {
                takePhoto().use { result ->

                    // Save the result to disk
                    val output = saveResult(result)

                    // If the result is a JPEG file, update EXIF metadata with orientation info
                    if (output.extension == "jpg") {
                        val exif = ExifInterface(output.absolutePath)
                        exif.setAttribute(
                            ExifInterface.TAG_ORIENTATION, result.orientation.toString()
                        )
                        exif.saveAttributes()
                        Timber.d("EXIF metadata saved: ${output.absolutePath}")
                    }

                    lifecycleScope.launch(Dispatchers.Main) {

                        when (relativeOrientation.value ?: 0) {
                            90, 270 -> binding.imageViewPreview.scaleType =
                                ImageView.ScaleType.CENTER_CROP
                            else -> binding.imageViewPreview.scaleType =
                                ImageView.ScaleType.CENTER_INSIDE
                        }

                        val uri = FileProvider.getUriForFile(
                            context!!,
                            "com.naposystems.pepito.provider",
                            output
                        )

                        Glide.with(binding.imageViewPreview)
                            .load(uri)
                            .into(binding.imageViewPreview)

                        if (binding.viewSwitcher.currentView.id == binding.containerCamera.id) {
                            binding.viewSwitcher.showNext()
                        }

                        shareViewModel.setMediaUri(output.absolutePath)

                        Timber.d("Image saved: ${output.absolutePath}")

                        it.isEnabled = true
                    }
                }
            }
        }
    }

    /** Opens the camera and returns the opened device (as the result of the suspend coroutine) */
    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Timber.w("Camera $cameraId has been disconnected")
                requireActivity().finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Timber.e("exc.message, exc: $exc")
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Starts a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine
     */
    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->

        // Create a capture session using the predefined targets; this also involves defining the
        // session state callback to be notified of when the session is ready
        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Timber.e("${exc.message}, $exc")
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Helper function used to capture a still image using the [CameraDevice.TEMPLATE_STILL_CAPTURE]
     * template. It performs synchronization between the [CaptureResult] and the [Image] resulting
     * from the single capture, and outputs a [CombinedCaptureResult] object.
     */
    private suspend fun takePhoto():
            CombinedCaptureResult = suspendCoroutine { cont ->

        // Flush any images left in the image reader
        @Suppress("ControlFlowWithEmptyBody")
        while (imageReader.acquireNextImage() != null) {
        }

        // Start a new image queue
        val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            Timber.d("Image available in queue: ${image.timestamp}")
            imageQueue.add(image)
        }, imageReaderHandler)

        val captureRequest = session.device.createCaptureRequest(
            CameraDevice.TEMPLATE_STILL_CAPTURE
        ).apply {
            set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH)
            set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
            set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO)
            set(CaptureRequest.JPEG_QUALITY, 80)
            addTarget(imageReader.surface)
        }
        session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                Timber.d("Capture result received: $resultTimestamp")

                // Set a timeout in case image captured is dropped from the pipeline
                val exc = TimeoutException("Image dequeuing took too long")
                val timeoutRunnable = Runnable { cont.resumeWithException(exc) }
                imageReaderHandler.postDelayed(timeoutRunnable, IMAGE_CAPTURE_TIMEOUT_MILLIS)

                // Loop in the coroutine's context until an image with matching timestamp comes
                // We need to launch the coroutine context again because the callback is done in
                //  the handler provided to the `capture` method, not in our coroutine context
                @Suppress("BlockingMethodInNonBlockingContext")
                lifecycleScope.launch(cont.context) {
                    while (true) {

                        // Dequeue images while timestamps don't match
                        val image = imageQueue.take()
                        // TODO(owahltinez): b/142011420
                        // if (image.timestamp != resultTimestamp) continue
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                            image.format != ImageFormat.DEPTH_JPEG &&
                            image.timestamp != resultTimestamp
                        ) continue
                        Timber.d("Matching image dequeued: ${image.timestamp}")

                        // Unset the image reader listener
                        imageReaderHandler.removeCallbacks(timeoutRunnable)
                        imageReader.setOnImageAvailableListener(null, null)

                        // Clear the queue of images, if there are left
                        while (imageQueue.size > 0) {
                            imageQueue.take().close()
                        }

                        // Compute EXIF orientation metadata
                        val rotation = relativeOrientation.value ?: 0
                        val mirrored = characteristics.get(CameraCharacteristics.LENS_FACING) ==
                                CameraCharacteristics.LENS_FACING_FRONT
                        val exifOrientation = computeExifOrientation(rotation, mirrored)

                        // Build the result and resume progress
                        cont.resume(
                            CombinedCaptureResult(
                                image, result, exifOrientation, imageReader.imageFormat
                            )
                        )

                        // There is no need to break out of the loop, this coroutine will suspend
                    }
                }
            }
        }, cameraHandler)
    }

    /** Helper function used to save a [CombinedCaptureResult] into a [File] */
    private suspend fun saveResult(result: CombinedCaptureResult): File = suspendCoroutine { cont ->
        when (result.format) {

            // When the format is JPEG or DEPTH JPEG we can simply save the bytes as-is
            ImageFormat.JPEG, ImageFormat.DEPTH_JPEG -> {
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                try {
                    photoFile = createFile("jpg")
                    FileOutputStream(photoFile).use { it.write(bytes) }
                    cont.resume(photoFile)
                } catch (exc: IOException) {
                    Timber.e("Unable to write JPEG image to file, ex: $exc")
                    cont.resumeWithException(exc)
                }
            }

            // When the format is RAW we use the DngCreator utility library
            ImageFormat.RAW_SENSOR -> {
                val dngCreator = DngCreator(characteristics, result.metadata)
                try {
                    val output = createFile("dng")
                    FileOutputStream(output).use { dngCreator.writeImage(it, result.image) }
                    cont.resume(output)
                } catch (exc: IOException) {
                    Timber.e("Unable to write DNG image to file, exc: $exc")
                    cont.resumeWithException(exc)
                }
            }

            // No other formats are supported by this sample
            else -> {
                val exc = RuntimeException("Unknown image format: ${result.image.format}")
                Timber.e("${exc.message} $exc")
                cont.resumeWithException(exc)
            }
        }
    }

    private fun createFile(extension: String): File {
        val timeStamp: String = System.currentTimeMillis().toString()

        val fileName = "${timeStamp}.$extension"
        val path = File(context!!.externalCacheDir!!, subFolder)
        if (!path.exists())
            path.mkdirs()

        // Create an image file name
        return File(path, fileName)
    }

    override fun onStop() {
        super.onStop()
        try {
            camera.close()
        } catch (exc: Throwable) {
            Timber.e("Error closing camera, exc: $exc")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
    }

}
