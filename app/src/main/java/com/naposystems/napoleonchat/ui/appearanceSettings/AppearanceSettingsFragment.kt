package com.naposystems.napoleonchat.ui.appearanceSettings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AppearanceSettingsFragmentBinding
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseViewModel
import com.naposystems.napoleonchat.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.napoleonchat.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.napoleonchat.ui.previewBackgroundChat.PreviewBackgroundChatViewModel
import com.naposystems.napoleonchat.ui.dialog.timeFormat.TimeFormatDialogFragment
import com.naposystems.napoleonchat.ui.dialog.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.LocaleHelper
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.dialog.PermissionDialogFragment
import com.naposystems.napoleonchat.utility.sharedViewModels.CameraSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.GallerySharedViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.yalantis.ucrop.UCrop
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AppearanceSettingsFragment : BaseFragment() {

    companion object {
        fun newInstance() = AppearanceSettingsFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        private const val FILE_EXTENSION = ".jpg"
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var handlerDialog: HandlerDialog

    private val viewModel: AppearanceSettingsViewModel by viewModels { viewModelFactory }

    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }

    private val previewBackgroundChatViewModel: PreviewBackgroundChatViewModel by viewModels {
        viewModelFactory
    }

    private val gallerySharedViewModel: GallerySharedViewModel by activityViewModels()
    private val cameraSharedViewModel: CameraSharedViewModel by activityViewModels()

    private lateinit var binding: AppearanceSettingsFragmentBinding
    private lateinit var fileName: String
    private var compressedFileName: String = ""
    private val actualChatBgFileName: String by lazy {
        "chat_background$FILE_EXTENSION"
    }
    private val subFolder: String by lazy {
        "chatBackground"
    }
    private var compressedFile: File? = null
    private var aspectRatioX: Float = 9f
    private var aspectRatioY: Float = 16f
    private val bitmapMaxWidth = 720
    private val bitmapMaxHeight = 1280
    private val imageCompression = 80

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { activity ->
            gallerySharedViewModel.uriImageSelected.observe(activity, Observer { uri ->
                if (uri != null) {
                    cropImage(uri)
                }
            })
            cameraSharedViewModel.uriImageTaken.observe(activity, Observer { uri ->
                if (uri != null) {
                    cropImage(uri)
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.appearance_settings_fragment,
            container,
            false
        )

        binding.lifecycleOwner = this

        binding.textViewLanguageSelected.text = getLanguageSelected()

        binding.optionColorScheme.setSafeOnClickListener { colorSchemeClickListener() }
        binding.imageButtonColorOptionEndIcon.setSafeOnClickListener { colorSchemeClickListener() }

        binding.optionDisplayFormat.setSafeOnClickListener { userDisplayFormatClickListener() }
        binding.imageButtonUserDisplayOptionEndIcon.setSafeOnClickListener {
            userDisplayFormatClickListener()
        }
        binding.optionTimeFormat.setSafeOnClickListener { timeFormatClickListener() }

        binding.optionLanguage.setSafeOnClickListener { languageClickListener() }
        binding.imageButtonLanguageOptionEndIcon.setSafeOnClickListener { languageClickListener() }

        binding.optionChatBackground.setSafeOnClickListener { chatBackgroundClickListener() }
        binding.imageButtonChatBackgroundOptionEndIcon.setSafeOnClickListener {
            chatBackgroundClickListener()
        }

        return binding.root
    }

    private fun getLanguageSelected(): String {
        return when (LocaleHelper.getLanguagePreference(requireContext())) {
            "de" -> "Deutsch"
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "it" -> "Italiano"
            "pt" -> "Português"
            else -> "English"
        }
    }

    private fun colorSchemeClickListener() {
        findNavController().navigate(
            AppearanceSettingsFragmentDirections
                .actionAppearanceSettingsFragmentToColorSchemeFragment()
        )
    }

    private fun languageClickListener() {
        val languageSelectionDialog = LanguageSelectionDialogFragment
            .newInstance(
                Constants.LocationSelectionLanguage.APPEARANCE_SETTINGS.location
            )
        languageSelectionDialog.show(childFragmentManager, "LanguageSelection")
    }

    private fun userDisplayFormatClickListener() {
        val userDisplayFormatDialog = UserDisplayFormatDialogFragment()
        userDisplayFormatDialog.setListener(object :
            UserDisplayFormatDialogFragment.UserDisplayFormatListener {
            override fun onUserDisplayChange() {
                viewModel.getUserDisplayFormat()
            }
        })
        userDisplayFormatDialog.show(childFragmentManager, "UserDisplayFormat")
    }

    private fun timeFormatClickListener() {
        val dialog = TimeFormatDialogFragment.newInstance()
        dialog.setListener(object : TimeFormatDialogFragment.TimeFormatListener {
            override fun onTimeFormatChange() {
                viewModel.getTimeFormat()
            }
        })
        dialog.show(childFragmentManager, "TimeFormat")
    }

    private fun chatBackgroundClickListener() {
        verifyCameraAndMediaPermission()
    }

    private fun verifyCameraAndMediaPermission() {
        validateStateOutputControl()
        Dexter.withContext(requireContext())
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        viewModel.getConversationBackground()
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        showDialogToInformPermission(
                            R.drawable.ic_camera_primary,
                            R.string.explanation_camera_and_storage_permission,
                            { openSetting() },
                            {}
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showDialogToInformPermission(
                        R.drawable.ic_camera_primary,
                        R.string.explanation_camera_and_storage_permission,
                        { token!!.continuePermissionRequest() },
                        { token!!.cancelPermissionRequest() }
                    )
                }
            }).check()
    }

    private fun showDialogToInformPermission(
        icon: Int,
        message: Int,
        accept: () -> Unit,
        cancel: () -> Unit
    ) {

        val dialog = PermissionDialogFragment.newInstance(
            icon,
            requireContext().resources.getString(message)
        )
        dialog.setListener(object : PermissionDialogFragment.OnDialogListener {
            override fun onAcceptPressed() {
                accept()
            }

            override fun onCancelPressed() {
                cancel()
            }
        })
        dialog.show(childFragmentManager, "Test")
    }

    private fun openSetting() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun openImageSelectorBottomSheet(showRestoreDefault: Boolean) {
        val title = requireContext().resources.getString(R.string.text_conversation_background)

        val dialog = ImageSelectorBottomSheetFragment.newInstance(
            title,
            Constants.LocationImageSelectorBottomSheet.APPEARANCE_SETTINGS.location,
            showRestoreDefault
        )
        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected(location: Int) {
                findNavController().navigate(
                    AppearanceSettingsFragmentDirections.actionAppearanceSettingsFragmentToConversationCameraFragment(
                        location = location
                    )
                )
            }

            override fun galleryOptionSelected(location: Int) {
                findNavController().navigate(
                    AppearanceSettingsFragmentDirections.actionAppearanceSettingsFragmentToAttachmentGalleryFoldersFragment(
                        null,
                        "",
                        Constants.LocationImageSelectorBottomSheet.APPEARANCE_SETTINGS.location
                    )
                )
            }

            override fun defaultOptionSelected(location: Int) {
                handlerDialog.generalDialog(
                    getString(R.string.text_select_default),
                    getString(R.string.text_message_restore_cover_photo),
                    true,
                    childFragmentManager
                ) {
                    previewBackgroundChatViewModel.updateChatBackground("")
                }
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        viewModel.getColorScheme()
        viewModel.getUserDisplayFormat()
        viewModel.getTimeFormat()
        baseViewModel.getOutputControl()

        viewModel.conversationBackground.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val showRestoreDefault = it.isNotEmpty()
                openImageSelectorBottomSheet(showRestoreDefault)
                viewModel.resetConversationBackgroundLiveData()
            }
        })

        viewModel.colorScheme.observe(viewLifecycleOwner) {
            binding.colorScheme = it
            binding.executePendingBindings()
        }

        viewModel.userDisplayFormat.observe(viewLifecycleOwner) {
            binding.userDisplayFormat = it
            binding.executePendingBindings()
        }

        viewModel.timeFormat.observe(viewLifecycleOwner) {
            binding.timeFormat = it
            binding.executePendingBindings()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    cropImage(Utils.getFileUri(requireContext(), fileName, subFolder))
                }
            }
            UCrop.REQUEST_CROP -> {
                requestCrop(resultCode)
            }
        }
    }

    private fun requestCrop(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                findNavController().navigate(
                    AppearanceSettingsFragmentDirections
                        .actionAppearanceSettingsFragmentToPreviewBackgroundChatFragment(
                            compressedFile?.name ?: ""
                        )
                )
                clearCache(requireContext())
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
    }

    private fun cropImage(sourceUri: Uri) {
        context?.let { context ->
            val title = context.resources.getString(R.string.text_conversation_background)

            compressedFileName = "${System.currentTimeMillis()}_compressed${FILE_EXTENSION}"

            compressedFile = FileManager.createFile(
                context,
                compressedFileName,
                Constants.CacheDirectories.CHAT_BACKGROUND.folder
            )

            val destinationUri = Uri.fromFile(compressedFile)

            val colorBackground =
                Utils.convertAttrToColorResource(context, R.attr.attrBackgroundColorPrimary)

            val options = UCrop.Options()
            options.setCompressionQuality(imageCompression)
            options.setToolbarColor(colorBackground)
            options.setStatusBarColor(colorBackground)
            options.setActiveWidgetColor(colorBackground)
            options.withAspectRatio(aspectRatioX, aspectRatioY)
            options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight)
            options.setToolbarTitle(title)
            options.setToolbarWidgetColor(ContextCompat.getColor(requireContext(), R.color.white))

            UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .start(context, this)
        }
    }

    private fun clearCache(context: Context) {
        val path = File(context.cacheDir!!.absolutePath, subFolder)
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()!!) {
                if (child.name != compressedFileName && child.name != actualChatBgFileName) {
                    child.delete()
                }
            }
        }
    }
}
