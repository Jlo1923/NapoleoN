package com.naposystems.napoleonchat.ui.contactProfile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ContactProfileFragmentBinding
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseViewModel
import com.naposystems.napoleonchat.ui.changeParams.ChangeFakeParamsDialogFragment
import com.naposystems.napoleonchat.ui.changeParams.ChangeParamsDialogFragment
import com.naposystems.napoleonchat.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.Utils.Companion.showSimpleSnackbar
import com.naposystems.napoleonchat.utility.sharedViewModels.camera.CameraShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.yalantis.ucrop.UCrop
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    @Inject
    lateinit var handlerNotificationChannel: HandlerNotificationChannel

    @Inject
    lateinit var handlerDialog: HandlerDialog

    private val viewModel: ContactProfileViewModel by viewModels { viewModelFactory }
    private val shareContactViewModel: ShareContactViewModel by viewModels { viewModelFactory }
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

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private var compressedFile: File? = null
    private lateinit var contact: ContactEntity
    private var contactSilenced: Boolean = false
    private lateinit var subFolder: String
    private lateinit var fileName: String
    private var aspectRatioX: Float = 1f
    private var aspectRatioY: Float = 1f
    private val bitmapMaxWidth = 1000
    private val bitmapMaxHeight = 1000
    private val imageCompression = 80

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let { activity ->

            galleryShareViewModel.uriImageSelected.observe(activity, { uri ->
                if (uri != null) {
                    cropImage(uri)
                }
            })
            cameraShareViewModel.uriImageTaken.observe(activity, { uri ->
                if (uri != null) {
                    cropImage(uri)
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.contact_profile_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.imageViewProfileContact.setSafeOnClickListener { showPreviewImage() }

        binding.optionCustomNotification.setOnClickListener { optionCustomNotification() }

        binding.switchSilenceConversation.setOnCheckedChangeListener(optionMessageClickListener())

        binding.optionRestoreContactChat.setSafeOnClickListener { optionRestoreContactChatClickListener() }

        binding.optionDeleteConversation.setSafeOnClickListener { optionDeleteConversationClickListener() }

        binding.optionBlockContact.setSafeOnClickListener { optionBlockContactClickListener() }

        binding.optionDeleteContact.setSafeOnClickListener { optionDeleteContactClickListener() }

        binding.imageButtonEditHeader.setSafeOnClickListener {
            subFolder = Constants.CacheDirectories.IMAGE_FAKE_CONTACT.folder
            verifyCameraAndMediaPermission()
        }

        binding.imageButtonChangeNameEndIcon.setSafeOnClickListener {
            val dialog = ChangeParamsDialogFragment.newInstance(
                args.contactId, Constants.ChangeParams.NAME_FAKE.option
            )
            dialog.show(childFragmentManager, "ChangeFakesDialog")
        }

        binding.optionChangeNickname.setSafeOnClickListener { openDialogNickFake() }

        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (args.contactId == it.contactId) {
                        if (contact.stateNotification) {
                            handlerNotificationChannel.deleteUserChannel(
                                contact.id,
                                contact.getNickName()
                            )
                        }
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }

        disposable.add(disposableContactBlockOrDelete)

        hideOptionForAndroidVersion()

        return binding.root
    }

    private fun openDialogNickFake() {
        val dialog = ChangeFakeParamsDialogFragment.newInstance(
            contact.id, contact.getNickName(), contact.stateNotification
        )
        dialog.show(childFragmentManager, "ChangeNickNameFakeDialog")
    }

    private fun showPreviewImage() {
        contactProfileShareViewModel.contact.value?.let { contact ->
            val extra = FragmentNavigatorExtras(
                binding.imageViewProfileContact to "transition_image_preview"
            )
            val titleToolbar = contact.getNickName()
            findNavController().navigate(
                ContactProfileFragmentDirections
                    .actionContactProfileFragmentToPreviewImageFragment(
                        contact, titleToolbar, null
                    ), extra
            )
        }
    }

    private fun optionBlockContactClickListener() {
        handlerDialog.generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            contactProfileShareViewModel.contact.value?.let { contact ->
                if (contact.statusBlocked) {
                    shareContactViewModel.unblockContact(contact.id)
                } else {
                    if (contact.stateNotification) {
                        handlerNotificationChannel.deleteUserChannel(
                            contact.id,
                            contact.getNickName()
                        )
                    }
                    shareContactViewModel.sendBlockedContact(contact)
                    findNavController().popBackStack(R.id.homeFragment, false)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun optionDeleteContactClickListener() {
        val getContact = contactProfileShareViewModel.contact.value
        getContact?.let { contact ->
            handlerDialog.generalDialog(
                getString(R.string.text_delete_contact),
                getString(R.string.text_wish_delete_contact),
                true,
                childFragmentManager
            ) {
                if (contact.stateNotification) {
                    handlerNotificationChannel.deleteUserChannel(
                        contact.id,
                        contact.getNickName()
                    )
                }
                shareContactViewModel.sendDeleteContact(contact)
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }
    }

    private fun optionDeleteConversationClickListener() {
        handlerDialog.generalDialog(
            getString(R.string.text_title_delete_conversation),
            getString(R.string.text_want_delete_conversation),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.deleteConversation(args.contactId)
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun optionRestoreContactChatClickListener() {
        handlerDialog.generalDialog(
            getString(R.string.text_reset_contact),
            getString(R.string.text_want_reset_contact),
            true,
            childFragmentManager
        ) {
            handlerNotificationChannel.deleteUserChannel(
                contact.id,
                contact.getNickName()
            )
            viewModel.restoreContact(args.contactId)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = contactProfileShareViewModel

        baseViewModel.getOutputControl()

        contactProfileShareViewModel.getLocalContact(args.contactId)

        viewModel.muteConversationWsError.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar {}
            }
        })
        viewModel.contactProfileWsError.observe(viewLifecycleOwner, {
            //show message error
            showSimpleSnackbar(binding.coordinator, it, 2)
        })

        contactProfileShareViewModel.contact.observe(viewLifecycleOwner, { contact ->
            contact?.let {
                checkSilenceConversation(contact.silenced)
                setTextToolbar(contact)
                setTextBlockedContact(contact.statusBlocked)
                setActiveCustomNotification(contact)
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
                data?.let { requestCrop(resultCode, data) }
            }
        }
    }

    private fun optionCustomNotification() {
        findNavController().navigate(
            ContactProfileFragmentDirections
                .actionContactProfileFragmentToCustomUserNotificationFragment(contact)
        )
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

    private fun setTextToolbar(contact: ContactEntity) {
        val text = if (contact.nicknameFake.isNotEmpty()) {
            contact.nicknameFake
        } else {
            contact.nickname
        }
        (activity as MainActivity).supportActionBar?.title = text
    }

    private fun setActiveCustomNotification(contactProfile: ContactEntity) {
        contact = contactProfile
        binding.editTextCustomNotification.isVisible = contactProfile.stateNotification
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
            Constants.CacheDirectories.IMAGE_FAKE_CONTACT.folder -> title =
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
                handlerDialog.generalDialog(
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

    private fun requestCrop(resultCode: Int, data: Intent) {
        if (resultCode == RESULT_OK) {
            try {
                val uri = UCrop.getOutput(data)
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }

                bitmap?.let {
                    val string64 = Utils.convertBitmapToBase64(bitmap)
                    viewModel.updateAvatarFakeContact(args.contactId, string64)
                    context?.let { context ->
                        clearCache(context)
                    }
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

    private fun hideOptionForAndroidVersion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.optionCustomNotification.isVisible = false
        }
    }
}
