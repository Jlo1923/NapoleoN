package com.naposystems.pepito.ui.profile

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
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
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.custom.AnimatedThreeVectorView
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

class ProfileFragment : BaseFragment() {

    companion object {
        fun newInstance() = ProfileFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_GALLERY_IMAGE = 2
        const val AVATAR_SUBFOLDER = "avatars"
        const val HEADER_SUBFOLDER = "headers"
        private const val FILE_EXTENSION = ".jpg"
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: ProfileFragmentBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var fileName: String
    private lateinit var subFolder: String
    private lateinit var animatedThreeEditName: AnimatedThreeVectorView
    private var aspectRatioX: Float = 1f
    private var aspectRatioY: Float = 1f
    private val bitmapMaxWidth = 1000
    private val bitmapMaxHeight = 1000
    private val imageCompression = 80

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onResume() {
        animatedThreeEditName.clearAnimation()
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.profile_fragment, container, false
        )
        binding.lifecycleOwner = this

        animatedThreeEditName = binding.imageButtonNameOptionEndIcon

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
            animatedThreeEditName.apply {
                if (!hasBeenInitialized) {
                    editToCancel(binding.editTextDisplayName)
                } else {
                    cancelToEdit(binding.editTextDisplayName)
                }
            }
        }

        binding.editTextDisplayName.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val newDisplayName = view.text.toString()

                animatedThreeEditName.cancelToHourglass()

                binding.editTextDisplayName.apply {
                    isEnabled = false
                }

                val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                    displayName = newDisplayName
                )

                viewModel.updateDisplayName(updateUserInfoReqDTO, {
                    animatedThreeEditName.hourglassToEdit()
                }, {
                    animatedThreeEditName.hourglassToCancel()
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
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ProfileViewModel::class.java)

        binding.viewModel = viewModel

        viewModelObservers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    cropImage(Utils.getFileUri(context!!, fileName, subFolder))
                }
            }
            REQUEST_GALLERY_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    try {
                        val imageUri = data!!.data
                        cropImage(imageUri!!)
                    }catch (e: Exception) {
                        Timber.e(e)
                    }
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
                        viewModel.user.value?.let {user ->
                            val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                                displayName = user.displayName,
                                avatar = Utils.convertBitmapToBase64(bitmap!!)
                            )

                            showAvatarProgress()
                            viewModel.updateAvatar(updateUserInfoReqDTO)
                        }
                    }
                    HEADER_SUBFOLDER -> {
                        val viewModelUser = viewModel.user.value!!

                        val user = User(
                            firebaseId = viewModelUser.firebaseId,
                            id = viewModelUser.id,
                            nickname = viewModelUser.nickname,
                            displayName = viewModelUser.displayName,
                            accessPin = viewModelUser.accessPin,
                            imageUrl = viewModelUser.imageUrl,
                            status = viewModelUser.status,
                            headerUri = uri.toString(),
                            chatBackground = viewModelUser.chatBackground,
                            type = viewModelUser.type,
                            createAt = viewModelUser.createAt
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
        animatedThreeEditName.clearAnimation()
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
                    firebaseId = viewModelUser.firebaseId,
                    id = viewModelUser.id,
                    nickname = it.nickname,
                    displayName = it.displayName,
                    accessPin = viewModelUser.accessPin,
                    imageUrl = it.avatarUrl,
                    status = it.status,
                    headerUri = viewModelUser.headerUri,
                    chatBackground = viewModelUser.chatBackground,
                    type = viewModelUser.type,
                    createAt = viewModelUser.createAt
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
                        Utils.showDialogToInformPermission(
                            context!!,
                            childFragmentManager,
                            R.drawable.ic_camera_primary,
                            R.string.explanation_camera_and_storage_permission,
                            { Utils.openSetting(context!!) },
                            {}
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Utils.showDialogToInformPermission(
                        context!!,
                        childFragmentManager,
                        R.drawable.ic_camera_primary,
                        R.string.explanation_camera_and_storage_permission,
                        { token!!.continuePermissionRequest() },
                        { token!!.cancelPermissionRequest() }
                    )
                }
            }).check()
    }

    private fun openImageSelectorBottomSheet() {

        var title = ""

        when (subFolder) {
            AVATAR_SUBFOLDER -> title =
                context!!.resources.getString(R.string.text_change_profile_photo)
            HEADER_SUBFOLDER -> title =
                context!!.resources.getString(R.string.text_change_cover_photo)
        }

        val dialog = ImageSelectorBottomSheetFragment.newInstance(title)
        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected() {
                fileName = "${System.currentTimeMillis()}.jpg"
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    Utils.getFileUri(context!!, fileName, subFolder)
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
                pickPhoto.type = "image/*"
                startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE)
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
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

        val path = File(context!!.cacheDir, subFolder)

        val destinationUri =
            Uri.fromFile(
                File(
                    path,
                    "${System.currentTimeMillis()}_compressed.${FILE_EXTENSION}"
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
        val path = File(context.cacheDir!!.absolutePath, subFolder)
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()!!) {
                child.delete()
            }
        }
    }

}
