package com.naposystems.pepito.ui.appearanceSettings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AppearanceSettingsFragmentBinding
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.baseFragment.BaseViewModel
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.pepito.ui.previewBackgroundChat.PreviewBackgroundChatViewModel
import com.naposystems.pepito.ui.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.dialog.PermissionDialogFragment
import com.naposystems.pepito.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
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

    private lateinit var viewModel: AppearanceSettingsViewModel

    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }

    private val previewBackgroundChatViewModel: PreviewBackgroundChatViewModel by viewModels {
        viewModelFactory
    }
    private val galleryShareViewModel : GalleryShareViewModel by activityViewModels()
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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryShareViewModel.uriImageSelected.observe(activity!!, Observer { uri ->
            if(uri != null) {
                cropImage(uri)
            }
        })
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

        binding.optionColorScheme.setOnClickListener(colorSchemeClickListener())
        binding.imageButtonColorOptionEndIcon.setOnClickListener(colorSchemeClickListener())

        binding.optionDisplayFormat.setOnClickListener(userDisplayFormatClickListener())
        binding.imageButtonUserDisplayOptionEndIcon.setOnClickListener(
            userDisplayFormatClickListener()
        )

        binding.optionLanguage.setOnClickListener(languageClickListener())
        binding.imageButtonLanguageOptionEndIcon.setOnClickListener(languageClickListener())

        binding.optionChatBackground.setOnClickListener(chatBackgroundClickListener())
        binding.imageButtonChatBackgroundOptionEndIcon.setOnClickListener(
            chatBackgroundClickListener()
        )

        return binding.root
    }

    private fun getLanguageSelected(): String {
        return when (LocaleHelper.getLanguagePreference(context!!)) {
            "de" -> "Deutsch"
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "it" -> "Italiano"
            "pt" -> "Português"
            else -> "English"
        }
    }

    private fun colorSchemeClickListener() = View.OnClickListener {
        findNavController().navigate(
            AppearanceSettingsFragmentDirections
                .actionAppearanceSettingsFragmentToColorSchemeFragment()
        )
    }

    private fun languageClickListener() = View.OnClickListener {
        val languageSelectionDialog = LanguageSelectionDialogFragment()
        languageSelectionDialog.show(childFragmentManager, "LanguageSelection")
    }

    private fun userDisplayFormatClickListener() = View.OnClickListener {
        val userDisplayFormatDialog = UserDisplayFormatDialogFragment()
        userDisplayFormatDialog.setListener(object :
            UserDisplayFormatDialogFragment.UserDisplayFormatListener {
            override fun onUserDisplayChange() {
                viewModel.getUserDisplayFormat()
            }
        })
        userDisplayFormatDialog.show(childFragmentManager, "UserDisplayFormat")
    }

    private fun chatBackgroundClickListener() = View.OnClickListener {
        verifyCameraAndMediaPermission()
    }

    private fun verifyCameraAndMediaPermission() {
        validateStateOutputControl()
        Dexter.withActivity(activity!!)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        openImageSelectorBottomSheet()
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
            context!!.resources.getString(message)
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
            Uri.fromParts("package", context!!.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun openImageSelectorBottomSheet() {
        val title = context!!.resources.getString(R.string.text_conversation_background)

        val dialog = ImageSelectorBottomSheetFragment.newInstance(
            title, Constants.LocationImageSelectorBottomSheet.APPEARANCE_SETTINGS.location
        )
        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected(location: Int) {
                fileName = "${System.currentTimeMillis()}${FILE_EXTENSION}"
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    Utils.getFileUri(context!!, fileName, subFolder)
                )
                if (takePictureIntent.resolveActivity(context!!.packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
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
                Utils.generalDialog(
                    getString(R.string.text_select_default),
                    getString(R.string.text_message_restore_image),
                    true,
                    childFragmentManager) {
                        previewBackgroundChatViewModel.updateChatBackground("")
                    }
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AppearanceSettingsViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getColorScheme()
        viewModel.getUserDisplayFormat()
        baseViewModel.getOutputControl()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    cropImage(Utils.getFileUri(context!!, fileName, subFolder))
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
                clearCache(context!!)
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
                Constants.NapoleonCacheDirectories.CHAT_BACKGROUND.folder
            )

            val destinationUri = Uri.fromFile(compressedFile)

            val valueColorBackground = TypedValue()
            context.theme.resolveAttribute(
                R.attr.attrBackgroundColorPrimary,
                valueColorBackground,
                true
            )

            val colorBackground = context.resources.getColor(valueColorBackground.resourceId)

            val options = UCrop.Options()
            options.setCompressionQuality(imageCompression)
            options.setToolbarColor(colorBackground)
            options.setStatusBarColor(colorBackground)
            options.setActiveWidgetColor(colorBackground)
            options.withAspectRatio(aspectRatioX, aspectRatioY)
            options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight)
            options.setToolbarTitle(title)
            options.setToolbarWidgetColor(ContextCompat.getColor(context!!, R.color.white))

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
