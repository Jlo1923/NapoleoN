package com.naposystems.pepito.ui.profile

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ProfileFragmentBinding
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.custom.AnimatedVectorView
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.dialog.PermissionDialogFragment
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_GALLERY_IMAGE = 2
        const val AVATAR_SUBFOLDER = "avatar"
        const val HEADER_SUBFOLDER = "header"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var binding: ProfileFragmentBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var fileName: String
    private lateinit var subFolder: String
    private lateinit var animatedEditName: AnimatedVectorView
    private var aspectRatioX: Float = 1f
    private var aspectRatioY: Float = 1f
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
            inflater, R.layout.profile_fragment, container, false
        )
        binding.lifecycleOwner = this

        animatedEditName = binding.imageButtonNameOptionEndIcon

        binding.floatingButtonProfileImage.setOnClickListener {
            subFolder = AVATAR_SUBFOLDER
            verifyCameraAndMediaPermission()
        }

        binding.imageViewProfileImage.setOnClickListener {
            subFolder = AVATAR_SUBFOLDER
            verifyCameraAndMediaPermission()
        }

        binding.imageButtonEditHeader.setOnClickListener {
            subFolder = HEADER_SUBFOLDER
            verifyCameraAndMediaPermission()
        }

        binding.imageButtonNameOptionEndIcon.setOnClickListener {
            animatedEditName.apply {
                if (!hasBeenInitialized) {
                    editToCancel()
                    binding.editTextDisplayName.apply {
                        isEnabled = true
                        isFocusable = true
                        requestFocus()
                        setSelection(this.text!!.length)
                        Utils.openKeyboard(this)
                    }
                } else {
                    cancelToEdit()
                    binding.editTextDisplayName.apply {
                        isEnabled = false
                        isFocusable = false
                    }
                }
            }
        }

        binding.editTextDisplayName.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val newDisplayName = view.text.toString()

                animatedEditName.cancelToHourglass()

                binding.editTextDisplayName.apply {
                    isEnabled = false
                }

                val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                    displayName = newDisplayName
                )

                viewModel.updateDisplayName(updateUserInfoReqDTO, {
                    animatedEditName.hourglassToEdit()
                }, {
                    animatedEditName.hourglassToCancel()
                    binding.editTextDisplayName.apply {
                        isEnabled = true
                    }
                })
                false
            } else
                true
        }

        binding.optionStatus.setOnClickListener(statusClickListener())
        binding.imageButtonStatusOptionEndIcon.setOnClickListener(statusClickListener())

        binding.optionBlockedContacts.setOnClickListener(blockedContactClickListener())
        binding.imageButtonBlockedContactsOptionEndIcon.setOnClickListener(
            blockedContactClickListener()
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)
        binding.viewModel = viewModel

        viewModelObservers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    cropImage(getCacheImagePath(fileName))
                }
            }
            REQUEST_GALLERY_IMAGE -> {
                if (resultCode == RESULT_OK) {
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
        if (resultCode == RESULT_OK) {
            val uri = UCrop.getOutput(data!!)
            try {
                val bitmap: Bitmap?

                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context!!.contentResolver, uri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(context!!.contentResolver, uri)
                }

                when (subFolder) {
                    AVATAR_SUBFOLDER -> {
                        val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                            avatar = Utils.convertBitmapToBase64(bitmap!!)!!
                        )

                        showAvatarProgress()
                        viewModel.updateAvatar(updateUserInfoReqDTO)

                    }
                    HEADER_SUBFOLDER -> {
                        val viewModelUser = viewModel.user.value!!

                        val user = User(
                            viewModelUser.firebaseId,
                            viewModelUser.id,
                            viewModelUser.nickname,
                            viewModelUser.displayName,
                            viewModelUser.accessPin,
                            viewModelUser.imageUrl,
                            viewModelUser.status,
                            uri.toString(),
                            viewModelUser.chatBackground
                        )

                        Glide.with(this)
                            .load(uri)
                            .into(binding.imageViewBackground)

                        viewModel.updateLocalUser(user)
                    }
                }

                clearCache(context!!)
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
    }

    override fun onDetach() {
        animatedEditName.clearAnimation()
        super.onDetach()
    }

    private fun statusClickListener() = View.OnClickListener {
        findNavController()
            .navigate(
                ProfileFragmentDirections
                    .actionProfileFragmentToStatusFragment(viewModel.user.value!!)
            )
    }

    private fun blockedContactClickListener() = View.OnClickListener {
        findNavController()
            .navigate(
                ProfileFragmentDirections.actionProfileFragmentToBlockedContactsFragment()
            )
    }

    private fun viewModelObservers() {

        viewModel.errorGettingLocalUser.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Utils.showSimpleSnackbar(
                    binding.coordinator,
                    getString(R.string.text_fail),
                    2
                )
            }
        })

        viewModel.userUpdated.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                hideAvatarProgressBar()

                val viewModelUser = viewModel.user.value!!

                val user = User(
                    viewModelUser.firebaseId,
                    viewModelUser.id,
                    it.nickname,
                    it.displayName,
                    viewModelUser.accessPin,
                    it.avatarUrl,
                    it.status,
                    viewModelUser.headerUri,
                    viewModelUser.chatBackground
                )

                viewModel.updateLocalUser(user)
            }
        })

        viewModel.errorUpdatingUser.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
                hideAvatarProgressBar()
            }
        })

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

    private fun openSetting() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context!!.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun openImageSelectorBottomSheet() {

        var title = ""

        when (subFolder) {
            AVATAR_SUBFOLDER -> title = context!!.resources.getString(R.string.text_change_profile_photo)
            HEADER_SUBFOLDER -> title = context!!.resources.getString(R.string.text_change_cover_photo)
        }

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
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }

            override fun galleryOptionSelected() {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE)
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    private fun getCacheImagePath(fileName: String): Uri {
        val path = File(activity!!.externalCacheDir!!.absolutePath, subFolder)
        if (!path.exists())
            path.mkdirs()
        val image = File(path, fileName)
        return getUriForFile(context!!, context!!.packageName + ".provider", image)
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

    private fun cropImage(sourceUri: Uri) {

        var title = ""

        when (subFolder) {
            AVATAR_SUBFOLDER -> {
                title = context!!.resources.getString(R.string.label_edit_photo)
                aspectRatioX = 1.0f
                aspectRatioY = 1.0f
            }
            HEADER_SUBFOLDER -> {
                title = context!!.resources.getString(R.string.label_edit_cover)
                aspectRatioX = 3.0f
                aspectRatioY = 2.0f
            }

        }

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

    private fun showAvatarProgress() {
        binding.imageViewProfileImage.alpha = 0.5f
        binding.progressAvatar.visibility = View.VISIBLE
    }

    private fun hideAvatarProgressBar() {
        binding.imageViewProfileImage.alpha = 1.0f
        binding.progressAvatar.visibility = View.GONE
    }

    private fun clearCache(context: Context) {
        val path = File(context.externalCacheDir!!.absolutePath, subFolder)
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()!!) {
                child.delete()
            }
        }
    }

}
