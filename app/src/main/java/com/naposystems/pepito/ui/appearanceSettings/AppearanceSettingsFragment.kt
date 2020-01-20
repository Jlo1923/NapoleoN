package com.naposystems.pepito.ui.appearanceSettings

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.ui.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.pepito.ui.profile.ProfileFragment
import com.naposystems.pepito.ui.userDisplayFormat.UserDisplayFormatDialogFragment
import com.naposystems.pepito.utility.LocaleHelper
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.dialog.PermissionDialogFragment
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AppearanceSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = AppearanceSettingsFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_GALLERY_IMAGE = 2
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: AppearanceSettingsViewModel
    private lateinit var binding: AppearanceSettingsFragmentBinding
    private lateinit var fileName: String
    private val subFolder: String by lazy {
        "chatBackground"
    }
    private var aspectRatioX: Float = 9f
    private var aspectRatioY: Float = 16f
    private val bitmapMaxWidth = 1000
    private val bitmapMaxHeight = 1000
    private val imageCompression = 80

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
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

        val dialog = ImageSelectorBottomSheetFragment.newInstance(title)
        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected() {
                fileName = "${System.currentTimeMillis()}.jpg"
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    getCacheImagePath(fileName)
                )
                if (takePictureIntent.resolveActivity(context!!.packageManager) != null) {
                    startActivityForResult(takePictureIntent, ProfileFragment.REQUEST_IMAGE_CAPTURE)
                }
            }

            override fun galleryOptionSelected() {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, ProfileFragment.REQUEST_GALLERY_IMAGE)
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    private fun getCacheImagePath(fileName: String): Uri {
        val path = File(activity!!.externalCacheDir!!.absolutePath, subFolder)
        if (!path.exists())
            path.mkdirs()
        val image = File(path, fileName)
        return FileProvider.getUriForFile(context!!, context!!.packageName + ".provider", image)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AppearanceSettingsViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getColorScheme()
        viewModel.getUserDisplayFormat()

        viewModel.chatBackgroundUpdated.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Utils.showSimpleSnackbar(
                    binding.coordinator,
                    getString(R.string.text_updated_successfully),
                    2
                )
                viewModel.resetChatBackgroundUpdated()
            } else if (it == false) {
                Utils.showSimpleSnackbar(
                    binding.coordinator,
                    getString(R.string.text_error_updating_conversation_background),
                    3
                )
                viewModel.resetChatBackgroundUpdated()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    cropImage(getCacheImagePath(fileName))
                }
            }
            REQUEST_GALLERY_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageUri = data!!.data
                    cropImage(imageUri!!)
                }
            }
            UCrop.REQUEST_CROP -> {
                requestCrop(resultCode, data)
            }
        }
    }

    private fun requestCrop(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = UCrop.getOutput(data!!)
            try {

                viewModel.updateChatBackground(uri.toString())

                clearCache(context!!)
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
    }

    private fun cropImage(sourceUri: Uri) {

        var title = context!!.resources.getString(R.string.text_conversation_background)

        val destinationUri =
            Uri.fromFile(
                File(
                    context!!.externalCacheDir,
                    queryName(context!!.contentResolver, sourceUri)
                )
            )
        val options = UCrop.Options()
        options.setCompressionQuality(imageCompression)
        options.setToolbarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
        options.setStatusBarColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
        options.setActiveWidgetColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
        options.withAspectRatio(aspectRatioX, aspectRatioY)
        options.withMaxResultSize(bitmapMaxWidth, bitmapMaxHeight)
        options.setToolbarTitle(title)
        options.setToolbarWidgetColor(ContextCompat.getColor(context!!, R.color.white))

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .start(context!!, this)
    }

    private fun clearCache(context: Context) {
        val path = File(context.externalCacheDir!!.absolutePath, subFolder)
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()!!) {
                child.delete()
            }
        }
    }

    private fun queryName(resolver: ContentResolver, uri: Uri): String {
        val returnCursor =
            resolver.query(uri, null, null, null, null)
        assert(returnCursor != null)
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

}
