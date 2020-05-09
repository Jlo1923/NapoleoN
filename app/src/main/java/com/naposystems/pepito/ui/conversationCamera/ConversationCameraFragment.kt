package com.naposystems.pepito.ui.conversationCamera

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
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
import kotlin.properties.Delegates


@SuppressLint("RestrictedApi")
class ConversationCameraFragment : Fragment(), VerticalSlider.Listener {

    private lateinit var binding: ConversationCameraFragmentBinding
    private val cameraShareViewModel: CameraShareViewModel by activityViewModels()
    private val args: ConversationCameraFragmentArgs by navArgs()

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
    private val mHandler: Handler by lazy {
        Handler()
    }

    private var flashMode by Delegates.observable(ImageCapture.FLASH_MODE_OFF) { _, _, newValue ->
        binding.imageButtonFlash.setImageResource(
            when (newValue) {
                ImageCapture.FLASH_MODE_ON -> R.drawable.ic_flash_on_black
                ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_flash_auto_black
                else -> R.drawable.ic_flash_off_black
            }
        )
    }

    companion object {
        private const val PHOTO_EXTENSION = ".jpg"
        private const val VIDEO_EXTENSION = ".mp4"
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

        binding.viewFinder.bindToLifecycle(viewLifecycleOwner)

        flashMode = ImageCapture.FLASH_MODE_OFF

        binding.verticalSlider.setListener(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        imageButtonSwitchCameraClickListener()

        imageButtonFlashClickListener()

        args.location.let { location ->
            when (location) {
                Constants.LocationImageSelectorBottomSheet.CONVERSATION.location -> {
                    imageButtonCameraTouchListener()
                }
                else -> {
                    binding.imageButtonCamera.setOnClickListener {
                        takePhoto()
                    }
                }
            }
        }


        viewFinderTouchListener()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun viewFinderTouchListener() {
        binding.viewFinder.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        val (x: Float, y: Float) = event.x to event.y

                        binding.lottieFocus.x = x - (binding.lottieFocus.width / 2)
                        binding.lottieFocus.y = y - (binding.lottieFocus.height / 2)
                        binding.lottieFocus.playAnimation()
                    }

                    return@setOnTouchListener false
                }
                else -> {
                    return@setOnTouchListener false
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun imageButtonCameraTouchListener() {
        binding.imageButtonCamera.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mHandler.postDelayed(mStartToRecordRunnable, 500)
                }
                MotionEvent.ACTION_UP -> {
                    mHandler.removeCallbacks(mStartToRecordRunnable)
                    if (!binding.viewFinder.isRecording) {
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
            flashMode = when (binding.viewFinder.flash) {
                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                else -> ImageCapture.FLASH_MODE_OFF
            }
            binding.viewFinder.flash = flashMode
        }
    }

    private fun imageButtonSwitchCameraClickListener() {
        binding.imageButtonSwitchCamera.setOnClickListener {
            binding.viewFinder.toggleCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.e("onDestroyView")
        CameraX.unbindAll()
    }

    private fun takePhoto() {
        photoFile = createFile(PHOTO_EXTENSION)

        binding.viewFinder.takePicture(
            photoFile,
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
        binding.imageButtonCamera.setBackgroundResource(R.drawable.bg_button_recoding)
        binding.imageButtonCamera.setImageResource(android.R.color.transparent)
        videoFile = createFile(VIDEO_EXTENSION)
        binding.viewFinder.startRecording(
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
            binding.imageButtonCamera.background =
                resources.getDrawable(R.drawable.bg_button_take_picture, requireContext().theme)
            binding.viewFinder.stopRecording()
            Timber.i("Video File stopped")
            recordingTime = 0L
        } catch (e: Exception) {
            Timber.e(e)
        }
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
            val minZoomRatio = binding.viewFinder.minZoomRatio
            val maxZoomRatio = binding.viewFinder.maxZoomRatio

            val finalZoomRatio = ((maxZoomRatio - minZoomRatio) * value + minZoomRatio)

            Timber.d("onSlide: $value, minZoomRatio: ${binding.viewFinder.minZoomRatio}, maxZoomRatio: ${binding.viewFinder.maxZoomRatio}, finalZoomRatio: $finalZoomRatio")

            if (binding.viewFinder.isZoomSupported) {
                binding.viewFinder.zoomRatio = finalZoomRatio
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al hacer zoom")
        }
    }
    //endregion
}
