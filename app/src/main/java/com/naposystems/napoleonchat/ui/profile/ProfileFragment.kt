package com.naposystems.napoleonchat.ui.profile

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
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ProfileFragmentBinding
import com.naposystems.napoleonchat.source.remote.dto.user.UserAvatarReqDTO
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseViewModel
import com.naposystems.napoleonchat.ui.changeParams.ChangeParamsDialogFragment
import com.naposystems.napoleonchat.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.napoleonchat.ui.logout.LogoutDialogFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.sharedViewModels.camera.CameraShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ProfileFragment : BaseFragment() {

    companion object {
        fun newInstance() = ProfileFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        private const val FILE_EXTENSION = ".jpg"
    }

    @Inject
    lateinit var handlerDialog: HandlerDialog

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: ProfileFragmentBinding
    private val viewModel: ProfileViewModel by viewModels { viewModelFactory }
    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }
    private val userProfileShareViewModel: UserProfileShareViewModel by viewModels {
        viewModelFactory
    }
    private val galleryShareViewModel: GalleryShareViewModel by activityViewModels()
    private val cameraShareViewModel: CameraShareViewModel by activityViewModels()
    private var compressedFile: File? = null
    private lateinit var fileName: String
    private lateinit var subFolder: String
    private var aspectRatioX: Float = 1f
    private var aspectRatioY: Float = 1f
    private val bitmapMaxWidth = 1000
    private val bitmapMaxHeight = 1000
    private val imageCompression = 80

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.profile_fragment, container, false
        )
        binding.lifecycleOwner = this

        binding.floatingButtonProfileImage.setSafeOnClickListener {
            subFolder = Constants.CacheDirectories.AVATAR.folder
            verifyCameraAndMediaPermission(Constants.LocationImageSelectorBottomSheet.PROFILE.location)
        }

        binding.imageViewProfileImage.setSafeOnClickListener {
            subFolder = Constants.CacheDirectories.AVATAR.folder
            verifyCameraAndMediaPermission(Constants.LocationImageSelectorBottomSheet.PROFILE.location)
        }

        binding.imageViewProfileImage.setSafeOnClickListener {
            viewModel.getUser()?.let { user ->
                val extra = FragmentNavigatorExtras(
                    binding.imageViewProfileImage to "transition_image_preview"
                )

                findNavController().navigate(
                    ProfileFragmentDirections
                        .actionProfileFragmentToPreviewImageFragment(
                            null, null, user
                        ), extra
                )
            }

        }

        binding.imageButtonEditHeader.setSafeOnClickListener {
            subFolder = Constants.CacheDirectories.HEADER.folder
            verifyCameraAndMediaPermission(Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location)
        }

        binding.imageButtonNameOptionEndIcon.setSafeOnClickListener {
            val dialog = ChangeParamsDialogFragment.newInstance(
                0, Constants.ChangeParams.NAME_USER.option
            )
            dialog.show(childFragmentManager, "ChangeFakesDialog")
        }

        binding.optionStatus.setSafeOnClickListener { statusClickListener() }
        binding.imageButtonStatusOptionEndIcon.setSafeOnClickListener { statusClickListener() }

        binding.optionBlockedContacts.setSafeOnClickListener { blockedContactClickListener() }
        binding.imageButtonBlockedContactsOptionEndIcon.setSafeOnClickListener {
            blockedContactClickListener()
        }

        binding.optionLogout.setSafeOnClickListener { logOutClickListener() }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryShareViewModel.uriImageSelected.observe(requireActivity(), Observer { uri ->
            if (uri != null) {
                cropImage(uri)
            }
        })
        cameraShareViewModel.uriImageTaken.observe(requireActivity(), Observer { uri ->
            if (uri != null) {
                cropImage(uri)
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.userEntity.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.user = user
                binding.executePendingBindings()
                if (user.imageUrl.isNotEmpty()) {
                    binding.imageViewProfileImage.background = null
                }
            }
        }

        baseViewModel.getOutputControl()

        viewModelObservers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    cropImage(Utils.getFileUri(requireContext(), fileName, subFolder))
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
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }

                when (subFolder) {
                    Constants.CacheDirectories.AVATAR.folder -> {
                        updateImageProfile(Utils.convertBitmapToBase64(bitmap!!))
                    }
                    Constants.CacheDirectories.HEADER.folder -> {
                        viewModel.getUser()?.let { user ->
                            user.headerUri = compressedFile?.name ?: ""

                            Glide.with(this)
                                .load(uri)
                                .into(binding.imageViewBackground)

                            userProfileShareViewModel.updateUserLocal(user)
                        }
                    }
                }

                clearCache(requireContext())
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
    }

    private fun updateImageProfile(avatar: String) {
        viewModel.getUser()?.let { user ->
            val updateUserInfoReqDTO = UserAvatarReqDTO(
                avatar = avatar
            )
            showAvatarProgress()
            userProfileShareViewModel.updateUserInfo(user, updateUserInfoReqDTO)
        }
    }

    private fun statusClickListener() {
        if (viewModel.getUser() != null) {
            findNavController()
                .navigate(
                    ProfileFragmentDirections
                        .actionProfileFragmentToStatusFragment(viewModel.getUser()!!)
                )
        }
    }

    private fun blockedContactClickListener() {
        findNavController()
            .navigate(
                ProfileFragmentDirections.actionProfileFragmentToBlockedContactsFragment()
            )
    }

    private fun logOutClickListener() {
        val logoutDialogFragment = LogoutDialogFragment.newInstance()
        logoutDialogFragment.setListener(object : LogoutDialogFragment.Listener {
            override fun logOutSuccessfully() {
                viewModel.disconnectSocket()
                findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLandingFragment())
            }
        })
        logoutDialogFragment.show(childFragmentManager, "logout")
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

        userProfileShareViewModel.userUpdated.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                hideAvatarProgressBar()
            }
        })

        userProfileShareViewModel.errorUpdatingUser.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar {}
                hideAvatarProgressBar()
            }
        })

    }

    private fun verifyCameraAndMediaPermission(location: Int) {
        validateStateOutputControl()
        Dexter.withContext(requireContext())
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        openImageSelectorBottomSheet(location)
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        Utils.showDialogToInformPermission(
                            requireContext(),
                            childFragmentManager,
                            R.drawable.ic_camera_primary,
                            R.string.explanation_camera_and_storage_permission,
                            { Utils.openSetting(requireContext()) },
                            {}
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    Utils.showDialogToInformPermission(
                        requireContext(),
                        childFragmentManager,
                        R.drawable.ic_camera_primary,
                        R.string.explanation_camera_and_storage_permission,
                        { token!!.continuePermissionRequest() },
                        { token!!.cancelPermissionRequest() }
                    )
                }
            }).check()
    }

    private fun openImageSelectorBottomSheet(location: Int) {

        val (title: String, showDefault: Boolean) = when (subFolder) {
            Constants.CacheDirectories.AVATAR.folder ->
                getString(R.string.text_change_profile_photo) to
                        (viewModel.getUser()?.imageUrl?.isNotEmpty() ?: false)
            else -> getString(R.string.text_change_cover_photo) to
                    (viewModel.getUser()?.headerUri?.isNotEmpty() ?: false)
        }

        val dialog = ImageSelectorBottomSheetFragment.newInstance(
            title, location, showDefault
        )

        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected(location: Int) {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToConversationCameraFragment(
                        location = location
                    )
                )
            }

            override fun galleryOptionSelected(location: Int) {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToAttachmentGalleryFoldersFragment(
                        null,
                        "",
                        location
                    )
                )
            }

            override fun defaultOptionSelected(location: Int) {
                val (dialogTitle: String, dialogMessage: String) = when (location) {
                    Constants.LocationImageSelectorBottomSheet.PROFILE.location ->
                        getString(R.string.text_profile_photo) to getString(R.string.text_message_restore_profile_photo)
                    else -> getString(R.string.text_cover_photo) to getString(R.string.text_message_restore_cover_photo)
                }
                handlerDialog.generalDialog(
                    dialogTitle,
                    dialogMessage,
                    true,
                    childFragmentManager
                ) {
                    when (location) {
                        Constants.LocationImageSelectorBottomSheet.PROFILE.location -> {
                            updateImageProfile("")
                        }
                        Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location -> {
                            viewModel.getUser()?.let { user ->
                                user.headerUri = ""
                                userProfileShareViewModel.updateUserLocal(user)
                            }
                        }
                    }
                }
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    private fun cropImage(sourceUri: Uri) {
        context?.let { context ->
            var title = ""

            when (subFolder) {
                Constants.CacheDirectories.AVATAR.folder -> {
                    title = context.resources.getString(R.string.label_edit_photo)
                    aspectRatioX = 1.0f
                    aspectRatioY = 1.0f
                }
                Constants.CacheDirectories.HEADER.folder -> {
                    title = context.resources.getString(R.string.label_edit_cover)
                    aspectRatioX = 3.0f
                    aspectRatioY = 2.0f
                }
            }

            compressedFile = FileManager.createFile(
                context,
                "${System.currentTimeMillis()}_compressed${FILE_EXTENSION}",
                subFolder
            )

            val destination = Uri.fromFile(compressedFile)

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
            options.setToolbarWidgetColor(ContextCompat.getColor(context, R.color.white))

            UCrop.of(sourceUri, destination)
                .withOptions(options)
                .start(context, this)
        }
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
            path.listFiles()?.forEach { child ->
                if (child.name != compressedFile?.name) {
                    child.delete()
                }
            }
        }
    }

}
