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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.naposystems.pepito.utility.Constants
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ProfileFragmentBinding
import com.naposystems.pepito.dto.profile.UpdateUserInfoReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.baseFragment.BaseViewModel
import com.naposystems.pepito.ui.custom.AnimatedThreeVectorView
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
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
    override lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: ProfileFragmentBinding
    private val viewModel: ProfileViewModel by viewModels { viewModelFactory }
    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }
    private val galleryShareViewModel: GalleryShareViewModel by activityViewModels()
    private var compressedFile: File? = null
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
            subFolder = Constants.NapoleonCacheDirectories.AVATAR.folder
            verifyCameraAndMediaPermission(Constants.LocationImageSelectorBottomSheet.PROFILE.location)
        }

        binding.imageViewProfileImage.setOnClickListener {
            subFolder = Constants.NapoleonCacheDirectories.AVATAR.folder
            verifyCameraAndMediaPermission(Constants.LocationImageSelectorBottomSheet.PROFILE.location)
        }

        binding.imageViewProfileImage.setOnClickListener {
            viewModel.user.value?.let { user ->
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

        binding.imageButtonEditHeader.setOnClickListener {
            subFolder = Constants.NapoleonCacheDirectories.HEADER.folder
            verifyCameraAndMediaPermission(Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryShareViewModel.uriImageSelected.observe(activity!!, Observer { uri ->
            if(uri != null) {
                cropImage(uri)
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel

        if (viewModel.user.value!!.imageUrl.isNotEmpty()) {
            binding.imageViewProfileImage.background = null
        }

        baseViewModel.getOutputControl()

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
                    Constants.NapoleonCacheDirectories.AVATAR.folder -> {
                        updateImageProfile(Utils.convertBitmapToBase64(bitmap!!))
                    }
                    Constants.NapoleonCacheDirectories.HEADER.folder -> {
                        val viewModelUser = viewModel.user.value!!

                        val user = User(
                            firebaseId = viewModelUser.firebaseId,
                            id = viewModelUser.id,
                            nickname = viewModelUser.nickname,
                            displayName = viewModelUser.displayName,
                            accessPin = viewModelUser.accessPin,
                            imageUrl = viewModelUser.imageUrl,
                            status = viewModelUser.status,
                            headerUri = compressedFile?.name ?: "",
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

    private fun updateImageProfile(avatar: String) {
        viewModel.user.value?.let { user ->
            val updateUserInfoReqDTO = UpdateUserInfoReqDTO(
                displayName = user.displayName,
                avatar = avatar
            )
            showAvatarProgress()
            viewModel.updateAvatar(updateUserInfoReqDTO)
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

    private fun verifyCameraAndMediaPermission(location: Int) {
        validateStateOutputControl()
        Dexter.withActivity(activity!!)
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        openImageSelectorBottomSheet(location)
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

    private fun openImageSelectorBottomSheet(location: Int) {

        var title = ""

        when (subFolder) {
            Constants.NapoleonCacheDirectories.AVATAR.folder -> title =
                context!!.resources.getString(R.string.text_change_profile_photo)
            Constants.NapoleonCacheDirectories.HEADER.folder -> title =
                context!!.resources.getString(R.string.text_change_cover_photo)
        }

        val dialog = ImageSelectorBottomSheetFragment.newInstance(
            title, location
        )

        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected(location: Int) {
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
                Utils.generalDialog(
                    getString(R.string.text_select_default),
                    getString(R.string.text_message_restore_image),
                    true,
                    childFragmentManager
                ) {
                    when (location) {
                        Constants.LocationImageSelectorBottomSheet.PROFILE.location -> {
                            updateImageProfile("")
                        }
                        Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location -> {
                            viewModel.user.value?.let { user ->
                                val userWithoutBanner = User(
                                    firebaseId = user.firebaseId,
                                    id = user.id,
                                    nickname = user.nickname,
                                    displayName = user.displayName,
                                    accessPin = user.accessPin,
                                    imageUrl = user.imageUrl,
                                    status = user.status,
                                    headerUri = "",
                                    chatBackground = user.chatBackground,
                                    type = user.type,
                                    createAt = user.createAt
                                )
                                viewModel.updateLocalUser(userWithoutBanner)
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
                Constants.NapoleonCacheDirectories.AVATAR.folder -> {
                    title = context.resources.getString(R.string.label_edit_photo)
                    aspectRatioX = 1.0f
                    aspectRatioY = 1.0f
                }
                Constants.NapoleonCacheDirectories.HEADER.folder -> {
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
