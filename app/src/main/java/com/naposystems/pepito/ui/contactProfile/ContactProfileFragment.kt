package com.naposystems.pepito.ui.contactProfile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import com.naposystems.pepito.ui.custom.AnimatedThreeVectorView
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.pepito.ui.profile.ProfileFragment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.camera.CameraShareViewModel
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

    private val galleryShareViewModel : GalleryShareViewModel by activityViewModels()
    private val cameraShareViewModel : CameraShareViewModel by activityViewModels()

    private val args: ContactProfileFragmentArgs by navArgs()
    private lateinit var binding: ContactProfileFragmentBinding
    private lateinit var animatedThreeEditName: AnimatedThreeVectorView
    private lateinit var animatedThreeEditNickName: AnimatedThreeVectorView

    private var compressedFile : File? = null
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
                if(uri != null) {
                    cropImage(uri)
                }
            })
            cameraShareViewModel.uriImageTaken.observe(activity, Observer { uri ->
                if(uri != null) {
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

        animatedThreeEditName = binding.imageButtonChangeNameEndIcon
        animatedThreeEditNickName = binding.imageButtonChangeNicknameEndIcon

        imageButtonChangeNameEndIconClickListener()

        setEditTextNameSetOnEditorActionListener()

        setEditTextNicknameSetOnEditorActionListener()

        imageButtonChangeNicknameEndIconClickListener()

        binding.imageViewProfileContact.setOnClickListener(showPreviewImage())

        binding.switchSilenceConversation.setOnCheckedChangeListener(optionMessageClickListener())

        binding.optionRestoreContactChat.setOnClickListener(optionRestoreContactChatClickListener())

        binding.optionDeleteConversation.setOnClickListener(optionDeleteConversationClickListener())

        binding.optionBlockContact.setOnClickListener(optionBlockContactClickListener())

        binding.imageButtonEditHeader.setOnClickListener {
            subFolder = Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder
            verifyCameraAndMediaPermission()
        }

        return binding.root
    }

    private fun showPreviewImage() = View.OnClickListener {
        viewModel.contact.value?.let { contact ->
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
                if (viewModel.contact.value!!.displayNameFake.isEmpty()) {
                    viewModel.contact.value!!.displayName
                } else {
                    viewModel.contact.value!!.displayNameFake
                }
            ),
            true,
            childFragmentManager
        ) {
            viewModel.contact.value?.let { contact ->
                if (contact.statusBlocked) {
                    shareContactViewModel.unblockContact(contact.id)
                } else {
                    shareContactViewModel.sendBlockedContact(contact)
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
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

    private fun imageButtonChangeNicknameEndIconClickListener() {
        binding.imageButtonChangeNicknameEndIcon.setOnClickListener {
            animatedThreeEditNickName.apply {
                if (hasBeenInitialized) {
                    binding.imageButtonChangeNameEndIcon.isEnabled = true
                    cancelToEdit(binding.editTextNickname)
                } else {
                    binding.imageButtonChangeNameEndIcon.isEnabled = false
                    editToCancel(binding.editTextNickname)
                }
            }
        }
    }

    private fun setEditTextNicknameSetOnEditorActionListener() {
        binding.editTextNickname.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.editTextName.text?.count()!! < 3) {
                    Utils.generalDialog(
                        getString(R.string.text_nickname_invalid),
                        getString(R.string.text_alert_nickname),
                        true,
                        childFragmentManager
                    ) { }
                } else {
                    binding.editTextNickname.apply {
                        isEnabled = false
                    }

                    animatedThreeEditNickName.cancelToHourglass()
                    viewModel.updateNicknameFakeContact(args.contactId, view.text.toString())
                    binding.editTextName.apply {
                        isEnabled = true
                    }

                    binding.editTextName.isFocusable = true
                    binding.imageButtonChangeNameEndIcon.isEnabled = true
                }
                false
            } else
                true
        }
    }

    private fun setEditTextNameSetOnEditorActionListener() {
        binding.editTextName.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                if (binding.editTextName.text?.count()!! < 3) {
                    Utils.generalDialog(
                        getString(R.string.text_name_invalid),
                        getString(R.string.text_alert_name),
                        true,
                        childFragmentManager
                    ) { }
                } else {
                    binding.editTextName.apply {
                        isEnabled = false
                    }

                    animatedThreeEditName.cancelToHourglass()
                    viewModel.updateNameFakeContact(args.contactId, view.text.toString())

                    binding.editTextName.apply {
                        isEnabled = true
                    }

                    binding.editTextNickname.isFocusable = true
                    binding.imageButtonChangeNicknameEndIcon.isEnabled = true
                }
                false
            } else
                true
        }
    }

    private fun imageButtonChangeNameEndIconClickListener() {
        binding.imageButtonChangeNameEndIcon.setOnClickListener {
            animatedThreeEditName.apply {
                if (hasBeenInitialized) {
                    binding.imageButtonChangeNicknameEndIcon.isEnabled = true
                    cancelToEdit(binding.editTextName)
                } else {
                    binding.imageButtonChangeNicknameEndIcon.isEnabled = false
                    editToCancel(binding.editTextName)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ContactProfileViewModel::class.java)

        try {
            shareContactViewModel = ViewModelProvider(activity!!, viewModelFactory)
                .get(ShareContactViewModel::class.java)
        } catch (e: Exception) {
            Timber.e(e)
        }

        binding.viewmodel = viewModel

        viewModel.getLocalContact(args.contactId)

        baseViewModel.getOutputControl()

        viewModel.muteConversationWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        viewModel.responseEditNameFake.observe(viewLifecycleOwner, Observer {
            if (it) {
                animatedThreeEditName.hourglassToEdit()
            }
        })

        viewModel.responseEditNicknameFake.observe(viewLifecycleOwner, Observer {
            if (it) {
                animatedThreeEditNickName.hourglassToEdit()
            }
        })

        viewModel.contact.observe(viewLifecycleOwner, Observer { contact ->
            checkSilenceConversation(contact.silenced)
            setTextToolbar(contact)
            setTextBlockedContact(contact.statusBlocked)
        })
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
        Dexter.withActivity(activity)
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
            Constants.NapoleonCacheDirectories.IMAGE_FAKE_CONTACT.folder -> title =
                context!!.resources.getString(R.string.text_change_cover_photo)
        }

        val dialog = ImageSelectorBottomSheetFragment.newInstance(
            title, Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location
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
                viewModel.contact.value?.let { contact ->
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
                    getString(R.string.text_message_restore_image),
                    true,
                    childFragmentManager) {
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
            path.listFiles()?.forEach {child ->
                if (child.name != compressedFile?.name) {
                    child.delete()
                }
            }
        }
    }
}
