package com.naposystems.pepito.ui.contactProfile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ContactProfileFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.baseFragment.BaseViewModel
import com.naposystems.pepito.ui.changeParams.ChangeParamsDialogFragment
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.camera.CameraShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ContactProfileFragment : BaseFragment() {

    companion object {
        fun newInstance() = ContactProfileFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        private const val FILE_EXTENSION = ".jpg"
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ContactProfileViewModel
    private lateinit var shareContactViewModel: ShareContactViewModel
    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }

    private val galleryShareViewModel: GalleryShareViewModel by activityViewModels()
    private val cameraShareViewModel: CameraShareViewModel by activityViewModels()
    private val contactProfileShareViewModel: ContactProfileShareViewModel by activityViewModels {
        viewModelFactory
    }

    private val args: ContactProfileFragmentArgs by navArgs()
    private lateinit var binding: ContactProfileFragmentBinding

    private var compressedFile: File? = null
    private var contactSilenced: Boolean = false
    private lateinit var subFolder: String
    private lateinit var fileName: String
    private var aspectRatioX: Float = 1f
    private var aspectRatioY: Float = 1f
    private val bitmapMaxWidth = 1000
    private val bitmapMaxHeight = 1000
    private val imageCompression = 80

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { activity ->
            galleryShareViewModel.uriImageSelected.observe(activity, Observer { uri ->
                if (uri != null) {
                    cropImage(uri)
                }
            })
            cameraShareViewModel.uriImageTaken.observe(activity, Observer { uri ->
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
            inflater, R.layout.contact_profile_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.imageViewProfileContact.setOnClickListener(showPreviewImage())

        binding.switchSilenceConversation.setOnCheckedChangeListener(optionMessageClickListener())

        binding.optionRestoreContactChat.setOnClickListener(optionRestoreContactChatClickListener())

        binding.optionDeleteConversation.setOnClickListener(optionDeleteConversationClickListener())

        binding.optionBlockContact.setOnClickListener(optionBlockContactClickListener())

        binding.optionDeleteContact.setOnClickListener(optionDeleteContactClickListener())

        binding.imageButtonEditHeader.setOnClickListener {
            subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
            verifyCameraAndMediaPermission()
        }

        binding.imageButtonChangeNameEndIcon.setOnClickListener {
            setupChangeFakeDialog(Constants.ChangeParams.NAME_FAKE.option)
        }

        binding.imageButtonChangeNicknameEndIcon.setOnClickListener {
            setupChangeFakeDialog(Constants.ChangeParams.NICKNAME_FAKE.option)
        }

        return binding.root
    }

    private fun setupChangeFakeDialog(option: Int) {
        val dialog = ChangeParamsDialogFragment.newInstance(
            args.contactId, option
        )
        dialog.show(childFragmentManager, "ChangeFakesDialog")
    }

    private fun showPreviewImage() = View.OnClickListener {
        contactProfileShareViewModel.contact.value?.let { contact ->
            val extra = FragmentNavigatorExtras(
                binding.imageViewProfileContact to "transition_image_preview"
            )
            val titleToolbar = if (contact.displayNameFake.isNotEmpty()) {
                contact.displayNameFake
            } else {
                contact.displayName
            }
            findNavController().navigate(
                ContactProfileFragmentDirections
                    .actionContactProfileFragmentToPreviewImageFragment(
                        contact, titleToolbar, null
                    ), extra
            )
        }
    }

    private fun optionBlockContactClickListener() = View.OnClickListener {
        Utils.generalDialog(
            getString(R.string.text_block_contact),
            getString(
                R.string.text_wish_block_contact,
                contactProfileShareViewModel.contact.value?.getName()
            ),
            true,
            childFragmentManager
        ) {
            contactProfileShareViewModel.contact.value?.let { contact ->
                if (contact.statusBlocked) {
                    shareContactViewModel.unblockContact(contact.id)
                } else {
                    shareContactViewModel.sendBlockedContact(contact)
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }
    }

    private fun optionDeleteContactClickListener() = View.OnClickListener {
        val getContact = contactProfileShareViewModel.contact.value
        getContact?.let { contact ->
            Utils.generalDialog(
                getString(R.string.text_delete_contact),
                getString(
                    R.string.text_wish_delete_contact,
                    if (contact.displayNameFake.isEmpty()) contact.displayName
                    else contact.displayNameFake
                ),
                true,
                childFragmentManager
            ) {
                shareContactViewModel.sendDeleteContact(contact)
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
    }

    private fun optionDeleteConversationClickListener() = View.OnClickListener {
        Utils.generalDialog(
            getString(R.string.text_title_delete_conversation),
            getString(R.string.text_want_delete_conversation),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.deleteConversation(args.contactId)
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun optionRestoreContactChatClickListener() = View.OnClickListener {
        Utils.generalDialog(
            getString(R.string.text_reset_contact),
            getString(R.string.text_want_reset_contact),
            true,
            childFragmentManager
        ) {
            viewModel.restoreContact(args.contactId)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ContactProfileViewModel::class.java)

        try {
            shareContactViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(ShareContactViewModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        binding.viewModel = contactProfileShareViewModel

        baseViewModel.getOutputControl()

        contactProfileShareViewModel.getLocalContact(args.contactId)

        viewModel.muteConversationWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        contactProfileShareViewModel.contact.observe(viewLifecycleOwner, Observer { contact ->
            contact?.let {
                checkSilenceConversation(contact.silenced)
                setTextToolbar(contact)
                setTextBlockedContact(contact.statusBlocked)
            }
        })
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
                requestCrop(resultCode)
            }
        }
    }

    private fun optionMessageClickListener() =
        CompoundButton.OnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    val dialog =
                        MuteConversationDialogFragment.newInstance(args.contactId, contactSilenced)
                    dialog.setListener(object :
                        MuteConversationDialogFragment.MuteConversationListener {
                        override fun onMuteConversationChange() {
                            checkSilenceConversation(contactSilenced)
                        }
                    })
                    dialog.show(childFragmentManager, "MuteConversation")
                } else {
                    viewModel.updateContactSilenced(args.contactId, contactSilenced)
                }
            }
        }

    private fun setTextBlockedContact(blocked: Boolean) {
        if (blocked) {
            binding.textViewLabelBlockContact.text =
                context?.getString(R.string.text_unblock_contact)
        } else {
            binding.textViewLabelBlockContact.text = context?.getString(R.string.text_block_contact)
        }
    }

    private fun checkSilenceConversation(silenced: Boolean) {
        binding.switchSilenceConversation.isChecked = silenced
        contactSilenced = silenced
    }

    private fun setTextToolbar(contact: Contact) {
        val text = if (contact.displayNameFake.isNotEmpty()) {
            contact.displayNameFake
        } else {
            contact.displayName
        }
        (activity as MainActivity).supportActionBar?.title = text
    }

    private fun verifyCameraAndMediaPermission() {
        validateStateOutputControl()
        Dexter.withContext(requireContext())
            .withPermissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        openImageSelectorBottomSheet()
                    } else if (report.isAnyPermissionPermanentlyDenied) {
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

    private fun openImageSelectorBottomSheet() {

        var title = ""

        when (subFolder) {
            Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder -> title =
                requireContext().resources.getString(R.string.text_change_cover_photo)
        }

        val dialog = ImageSelectorBottomSheetFragment.newInstance(
            title, Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location, false
        )
        dialog.setListener(object : ImageSelectorBottomSheetFragment.OnOptionSelected {
            override fun takeImageOptionSelected(location: Int) {
                findNavController().navigate(
                    ContactProfileFragmentDirections.actionContactProfileFragmentToConversationCameraFragment(
                        location = location
                    )
                )
            }

            override fun galleryOptionSelected(location: Int) {
                contactProfileShareViewModel.contact.value?.let { contact ->
                    findNavController().navigate(
                        ContactProfileFragmentDirections.actionContactProfileFragmentToAttachmentGalleryFoldersFragment(
                            contact,
                            "",
                            Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location
                        )
                    )
                }
            }

            override fun defaultOptionSelected(location: Int) {
                Utils.generalDialog(
                    getString(R.string.text_select_default),
                    getString(R.string.text_message_restore_cover_photo),
                    true,
                    childFragmentManager
                ) {
                    viewModel.restoreImageByContact(args.contactId)
                }
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    private fun cropImage(sourceUri: Uri) {
        context?.let { context ->
            val title = context.resources.getString(R.string.label_edit_cover)

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

    private fun requestCrop(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            try {
                viewModel.updateAvatarFakeContact(args.contactId, compressedFile?.name ?: "")
                context?.let { context ->
                    clearCache(context)
                }
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
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
