package com.naposystems.pepito.ui.conversationCamera

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationCameraFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.custom.cameraButton.CameraButton
import com.naposystems.pepito.ui.custom.verticalSlider.VerticalSlider
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
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
class ConversationCameraFragment : Fragment(), VerticalSlider.Listener,
    CameraButton.CameraButtonListener {

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
    private var recordingTime: Long = TimeUnit.MINUTES.toMillis(1)
    private var mStartToRecordRunnable: Runnable = Runnable { startRecording() }
    private lateinit var mRecordingTimeRunnable: Runnable

    private var isBackPressed = false

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
        private const val PHOTO_EXTENSION = "jpg"
        private const val VIDEO_EXTENSION = "mp4"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(binding.viewFinder.isRecording){
                binding.viewFinder.stopRecording()
                isBackPressed = true
            } else {
                findNavController().popBackStack()
            }
        }
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

        binding.imageButtonLock.post {
            with(binding.imageButtonCamera) {
                setListener(this@ConversationCameraFragment)
                setMaxY(binding.imageButtonLock.y)
            }
        }

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
                    binding.imageButtonCamera.setAllowSlide(true)
                }
                else -> {
                    binding.imageButtonCamera.setAllowSlide(false)
                }
            }
        }

        binding.imageButtonCamera.setOnClickListener {
            Timber.d("setOnClickListener")
            if (!binding.viewFinder.isRecording) {
                takePhoto()
            }

            if (binding.viewFinder.isRecording && binding.imageButtonCamera.isLocked()) {
                stopRecording()
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

    override fun onStop() {
        super.onStop()
        if (binding.viewFinder.isRecording) {
            binding.viewFinder.stopRecording()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.e("onDestroyView")
        if (binding.viewFinder.isRecording) {
            binding.viewFinder.stopRecording()
        }
//        CameraX.unbindAll()
    }

    private fun takePhoto() {
        binding.imageButtonCamera.isEnabled = false
        photoFile = createFile(PHOTO_EXTENSION)

        val optionsCapture = ImageCapture.OutputFileOptions.Builder(
            photoFile
        ).build()

        binding.viewFinder.takePicture(
            optionsCapture,
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
                                    status = Constants.AttachmentStatus.SENDING.status,
                                    extension = PHOTO_EXTENSION,
                                    duration = 0L
                                )


                                findNavController().navigate(
                                    ConversationCameraFragmentDirections.actionConversationCameraFragmentToAttachmentPreviewFragment(
                                        attachment,
                                        0,
                                        args.quote,
                                        args.message
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
        startRunnableTimer()
        binding.imageButtonCamera.setBackgroundResource(R.drawable.bg_button_recoding)
        binding.imageButtonCamera.setImageResource(android.R.color.transparent)

        if (flashMode == ImageCapture.FLASH_MODE_ON) {
            binding.viewFinder.enableTorch(true)
        }

        binding.imageButtonSwitchCamera.visibility = View.GONE
        binding.imageButtonFlash.visibility = View.GONE

        val animationSlideIn: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_up)

        animationSlideIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
                // Intentionally empty
            }

            override fun onAnimationEnd(animation: Animation?) {
                with(binding.imageButtonCamera) {
                    setListener(this@ConversationCameraFragment)
                    setMaxY(binding.imageButtonLock.y)

                    val animationScale: AnimatorSet =
                        AnimatorInflater.loadAnimator(
                            requireContext(),
                            R.animator.animator_scale_up_down_infinite
                        ) as AnimatorSet

                    animationScale.setTarget(binding.imageButtonLock)
                    animationScale.start()
                }
            }

            override fun onAnimationStart(animation: Animation?) {
                // Intentionally empty
            }
        })

        binding.imageButtonLock.visibility = View.VISIBLE
        binding.imageButtonLock.startAnimation(animationSlideIn)

        videoFile = createFile(VIDEO_EXTENSION)
        binding.viewFinder.startRecording(
            videoFile,
            mainExecutor,
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(file: File) {

                    if (!isBackPressed){
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
                            status = Constants.AttachmentStatus.SENDING.status,
                            extension = VIDEO_EXTENSION,
                            duration = 0L
                        )

                        mHandler.removeCallbacks(mRecordingTimeRunnable)
                        hideRecordingTime()

                        findNavController().navigate(
                            ConversationCameraFragmentDirections.actionConversationCameraFragmentToAttachmentPreviewFragment(
                                attachment,
                                0,
                                args.quote,
                                args.message
                            )
                        )
                    } else {
                        if (videoFile.exists()) videoFile.delete()
                        findNavController().popBackStack()
                    }
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
    }

    private fun startRunnableTimer() {
        mRecordingTimeRunnable = Runnable {
            binding.lottieRecording.visibility = View.VISIBLE
            binding.textViewRecordingTime.apply {
                visibility = View.VISIBLE
                text = Utils.getDuration(recordingTime, showHours = false)
            }

            val oneSecond = TimeUnit.SECONDS.toMillis(1)
            recordingTime -= oneSecond
            Timber.d("recordingTime: $recordingTime")
            if (recordingTime == 1000L) {
                stopRecording()
            } else {
                mHandler.postDelayed(mRecordingTimeRunnable, oneSecond)
            }
        }
        mHandler.postDelayed(mRecordingTimeRunnable, 0)
    }

    private fun hideRecordingTime() {
        binding.lottieRecording.visibility = View.GONE
        binding.textViewRecordingTime.visibility = View.GONE
    }

    private fun stopRecording() {
        try {
            binding.imageButtonCamera.isEnabled = false
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
            else -> Constants.NapoleonCacheDirectories.VIDEOS.folder
        }

        fileName = "${timeStamp}.$extension"
        // Create an image file name
        return FileManager.createFile(requireContext(), fileName, subFolder)
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

    //region Implementation CameraButton.CameraButtonListener
    override fun startToRecord() {
        startRecording()
    }

    override fun hasLocked() {
        binding.imageButtonLock.visibility = View.GONE
        binding.imageButtonCamera.setBackgroundResource(R.drawable.bg_button_take_picture)
        binding.imageButtonCamera.setImageResource(R.drawable.ic_stop_black)
    }

    override fun actionUp(hasLocked: Boolean) {
        if (!hasLocked && binding.viewFinder.isRecording) {
            stopRecording()
        }
    }

    //endregion
}
