package com.naposystems.pepito.ui.contactProfile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ContactProfileFragmentBinding
import com.naposystems.pepito.ui.custom.AnimatedVectorView
import com.naposystems.pepito.ui.imagePicker.ImageSelectorBottomSheetFragment
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.pepito.ui.profile.ProfileFragment
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import com.yalantis.ucrop.UCrop
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.profile_fragment.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ContactProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ContactProfileFragment()
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_GALLERY_IMAGE = 2
        const val HEADER_SUBFOLDER = "headers"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactProfileViewModel
    private val args: ContactProfileFragmentArgs by navArgs()
    private lateinit var binding: ContactProfileFragmentBinding
    private lateinit var animatedEditName: AnimatedVectorView
    private lateinit var animatedEditNickName: AnimatedVectorView

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.contact_profile_fragment, container, false
        )

        binding.lifecycleOwner = this

        animatedEditName = binding.imageButtonChangeNameEndIcon
        animatedEditNickName = binding.imageButtonChangeNicknameEndIcon

        binding.imageButtonChangeNameEndIcon.setOnClickListener {
            animatedEditName.apply {
                if (hasBeenInitialized) {
                    binding.imageButtonChangeNicknameEndIcon.isEnabled = true
                    cancelToEdit(binding.editTextName)
                } else {
                    binding.imageButtonChangeNicknameEndIcon.isEnabled = false
                    editToCancel(binding.editTextName)
                }
            }
        }

        binding.editTextName.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                if (binding.editTextName.text?.count()!! < 3) {
                    Utils.generalDialog(
                        getString(R.string.text_name_invalid),
                        getString(R.string.text_alert_name),
                        childFragmentManager
                    ) { }
                } else {
                    binding.editTextName.apply {
                        isEnabled = false
                    }

                    animatedEditName.cancelToHourglass()
                    viewModel.updateNameFakeLocalContact(args.idContact, view.text.toString())

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

        binding.editTextNickname.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.editTextName.text?.count()!! < 3) {
                    Utils.generalDialog(
                        getString(R.string.text_nickname_invalid),
                        getString(R.string.text_alert_nickname),
                        childFragmentManager
                    ) { }
                } else {
                    binding.editTextNickname.apply {
                        isEnabled = false
                    }

                    animatedEditNickName.cancelToHourglass()
                    viewModel.updateNicknameFakeLocalContact(args.idContact, view.text.toString())
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

        binding.imageButtonChangeNicknameEndIcon.setOnClickListener {
            animatedEditNickName.apply {
                if (hasBeenInitialized) {
                    binding.imageButtonChangeNameEndIcon.isEnabled = true
                    cancelToEdit(binding.editTextNickname)
                } else {
                    binding.imageButtonChangeNameEndIcon.isEnabled = false
                    editToCancel(binding.editTextNickname)
                }
            }
        }

        binding.switchSilenceConversation.setOnCheckedChangeListener(optionMessageClickListener())

        binding.optionRestoreContactChat.setOnClickListener {
            Utils.generalDialog(
                getString(R.string.text_reset_contact),
                getString(R.string.text_want_reset_contact),
                childFragmentManager
            ) {
                viewModel.restoreLocalContact(args.idContact)
            }
        }

        binding.optionDeleteConversation.setOnClickListener {
            Utils.generalDialog(
                getString(R.string.text_delete_conversation),
                getString(R.string.text_want_delete_conversation),
                childFragmentManager
            ) {
                viewModel.deleteConversation(args.idContact)
            }
        }

        binding.imageButtonEditHeader.setOnClickListener {
            subFolder = HEADER_SUBFOLDER
            verifyCameraAndMediaPermission()
        }

        return binding.root
    }

    private fun actionVectorView(animatedEditText: AnimatedVectorView, editText: EditText) {
        animatedEditText.apply {
            if (!hasBeenInitialized) {
                editToCancel(editText)
            } else {
                cancelToEdit(editText)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ContactProfileViewModel::class.java)

        binding.viewmodel = viewModel

        viewModel.getLocalContact(args.idContact)

        viewModel.muteConversationWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        viewModel.responseEditNameFake.observe(viewLifecycleOwner, Observer {
            if (it) {
                animatedEditName.hourglassToEdit()
            }
        })

        viewModel.responseEditNicknameFake.observe(viewLifecycleOwner, Observer {
            if (it) {
                animatedEditNickName.hourglassToEdit()
            }
        })

        viewModel.contact.observe(viewLifecycleOwner, Observer {
            checkSilenceConversation(it.silenced)
            val text = if (it.displayNameFake.isNotEmpty()) {
                it.displayNameFake
            } else {
                it.displayName
            }
            setTextToolbar(text)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    cropImage(Utils.getCacheImagePath(context!!, fileName, subFolder))
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

    private fun optionMessageClickListener() =
        CompoundButton.OnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (isChecked) {
                    val dialog =
                        MuteConversationDialogFragment.newInstance(args.idContact, contactSilenced)
                    dialog.setListener(object :
                        MuteConversationDialogFragment.MuteConversationListener {
                        override fun onMuteConversationChange() {
                            checkSilenceConversation(contactSilenced)
                        }
                    })
                    dialog.show(childFragmentManager, "MuteConversation")
                } else {
                    viewModel.updateContactSilenced(args.idContact, contactSilenced)
                }
            }
        }

    private fun checkSilenceConversation(silenced: Boolean) {
        binding.switchSilenceConversation.isChecked = silenced
        contactSilenced = silenced
    }

    private fun setTextToolbar(text: String) {
        (activity as MainActivity).supportActionBar?.title = text
    }

    private fun verifyCameraAndMediaPermission() {
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
                    Utils.getCacheImagePath(context!!, fileName, subFolder)
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
                startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE)
            }
        })
        dialog.show(childFragmentManager, "BottomSheetOptions")
    }

    private fun cropImage(sourceUri: Uri) {

        val title = context!!.resources.getString(R.string.label_edit_cover)
        val destinationUri =
            Uri.fromFile(
                File(
                    context!!.externalCacheDir,
                    Utils.queryName(context!!.contentResolver, sourceUri)
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

    private fun requestCrop(resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val uri = UCrop.getOutput(data!!)
            try {
                viewModel.updateAvatarFakeLocalContact(args.idContact, uri.toString())
            } catch (ex: IOException) {
                Timber.e(ex)
            }
        }
    }
}
