package com.naposystems.pepito.ui.conversationCamera

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import androidx.camera.core.*
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationCameraFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.custom.verticalSlider.VerticalSlider
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.camera.CameraShareViewModel
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@SuppressLint("RestrictedApi")
class ConversationCameraFragment : Fragment(), VerticalSlider.Listener {

    private lateinit var binding: ConversationCameraFragmentBinding
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
    private val cameraShareViewModel: CameraShareViewModel by activityViewModels()
    private lateinit var listener: ScaleGestureDetector.SimpleOnScaleGestureListener
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null
    private var camera: Camera? = null
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
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
        Timber.d("onCreate")
        mainExecutor = ContextCompat.getMainExecutor(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView")
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.conversation_camera_fragment,
            container,
            false
        )

        binding.verticalSlider.setListener(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupListenerZoom()

        binding.viewFinder.post {
            startCamera()
        }

        imageButtonSwitchCameraClickListener()

        imageButtonFlashClickListener()

        imageButtonCameraTouchListener()

        viewFinderTouchListener()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewFinderTouchListener() {
        binding.viewFinder.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        val factory = SurfaceOrientedMeteringPointFactory(
                            binding.viewFinder.width.toFloat(),
                            binding.viewFinder.height.toFloat()
                        )

                        val (x: Float, y: Float) = event.x to event.y

                        val point = factory.createPoint(x, y)

                        val action = FocusMeteringAction
                            .Builder(point)
                            .build()
                        cameraControl?.startFocusAndMetering(action)

                        binding.lottieFocus.x = x - (binding.lottieFocus.width / 2)
                        binding.lottieFocus.y = y - (binding.lottieFocus.height / 2)
                        binding.lottieFocus.playAnimation()
                    }

                    return@setOnTouchListener true
                }
                else -> {
                    scaleGestureDetector.onTouchEvent(event)
                    return@setOnTouchListener true
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun imageButtonCameraTouchListener() {
        binding.imageButtonCamera.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (args.location == Constants.LocationImageSelectorBottomSheet.CONVERSATION.location) {
                        mHandler.postDelayed(mStartToRecordRunnable, 500)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mHandler.removeCallbacks(mStartToRecordRunnable)
                    if (!isRecording) {
                        takePhoto()
                    } else {
                        stopRecording()
                    }
                }
            }
            true
        }
    }

    private fun imageButtonFlashClickListener() {
        binding.imageButtonFlash.setOnClickListener {
            torchEnable = !torchEnable
            imageCapture!!.flashMode =
                if (torchEnable) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            binding.imageButtonFlash.setImageDrawable(
                requireContext().resources.getDrawable(
                    if (torchEnable) R.drawable.ic_flash_on_black else R.drawable.ic_flash_off_black,
                    requireContext().theme
                )
            )
        }
    }

    private fun imageButtonSwitchCameraClickListener() {
        binding.imageButtonSwitchCamera.setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            binding.viewFinder.post {
                startCamera()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.e("onDestroyView")
        try {
            cameraExecutor.shutdown()
            videoCapture?.stopRecording()
            videoCapture?.clear()
            if (::mRecordingTimeRunnable.isInitialized) {
                mHandler.removeCallbacks(mRecordingTimeRunnable)
            }
            mHandler.removeCallbacks(mStartToRecordRunnable)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun startCamera() {
        try {// Get screen metrics used to setup camera for full screen resolution
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

                    camera?.let { camera ->
                        cameraControl = camera.cameraControl
                        cameraInfo = camera.cameraInfo
                    }

                    // Default PreviewSurfaceProvider
                    val surfaceProvider = binding.viewFinder.createSurfaceProvider(cameraInfo)
                    preview?.setSurfaceProvider(surfaceProvider)
                } catch (exc: Exception) {
                    Timber.e("Use case binding failed, exc: $exc")
                }

            }, ContextCompat.getMainExecutor(requireContext()))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setupListenerZoom() {
        listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                val currentZoomRatio: Float = cameraInfo!!.zoomState.value?.zoomRatio ?: 0F
                val delta = detector?.scaleFactor
                cameraControl?.setZoomRatio(currentZoomRatio * delta!!)
                return true
            }
        }
        scaleGestureDetector = ScaleGestureDetector(context, listener)
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
                    args.location.let { location ->
                        when (location) {
                            Constants.LocationImageSelectorBottomSheet.CONVERSATION.location -> {
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
                            else -> {
                                context?.let { context ->
                                    val uri = Utils.getFileUri(
                                        context = context,
                                        fileName = photoFile.name,
                                        subFolder = Constants.NapoleonCacheDirectories.IMAGES.folder
                                    )

                                    with(cameraShareViewModel) {
                                        setImageUriTaken(uri)
                                        resetUriImageTaken()
                                    }
                                    when (location) {
                                        Constants.LocationImageSelectorBottomSheet.PROFILE.location,
                                        Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location -> {
                                            findNavController().popBackStack(
                                                R.id.profileFragment,
                                                false
                                            )
                                        }
                                        Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location -> {
                                            findNavController().popBackStack(
                                                R.id.contactProfileFragment,
                                                false
                                            )
                                        }
                                        else -> {
                                            findNavController().popBackStack(
                                                R.id.appearanceSettingsFragment,
                                                false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
        try {
            binding.imageButtonCamera.setBackgroundColor(
                resources.getColor(
                    android.R.color.transparent,
                    requireContext().theme
                )
            )
            videoCapture?.stopRecording()
            isRecording = false
            Timber.i("Video File stopped")
            recordingTime = 0L
        } catch (e: Exception) {
            Timber.e(e)
        }
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
        path = File(requireContext().cacheDir!!, subFolder)
        if (!path.exists())
            path.mkdirs()

        // Create an image file name
        return File(path, fileName)
    }

    //region Implementation VerticalSlider.Listener
    override fun onSlide(value: Float) {
        try {
            cameraControl?.setLinearZoom(value)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
    //endregion
}
