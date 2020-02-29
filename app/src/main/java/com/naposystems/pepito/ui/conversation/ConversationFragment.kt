package com.naposystems.pepito.ui.conversation

import android.Manifest
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationActionBarBinding
import com.naposystems.pepito.databinding.ConversationFragmentBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.attachment.AttachmentDialogFragment
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.adapters.verifyPermission
import com.naposystems.pepito.utility.itemAnimators.SlideInUpAnimator
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationFragment : Fragment(), MediaPlayerManager.Listener {

    companion object {
        fun newInstance() = ConversationFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private val viewModel: ConversationViewModel by viewModels {
        viewModelFactory
    }
    private val shareViewModel: ConversationShareViewModel by activityViewModels()

    private lateinit var actionBarCustomView: ConversationActionBarBinding
    private lateinit var binding: ConversationFragmentBinding
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val args: ConversationFragmentArgs by navArgs()
    private var isEditTextFilled: Boolean = false

    private val mediaPlayerManager: MediaPlayerManager by lazy {
        MediaPlayerManager(context!!)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        inflateCustomActionBar(inflater)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.contact = args.contact

        setupAdapter()

        Handler().postDelayed({
            binding.textViewUserStatus.isSelected = true
        }, TimeUnit.SECONDS.toMillis(2))

        binding.inputPanel.getFloatingActionButton().setOnClickListener {

            if (!binding.inputPanel.getFloatingActionButton().isShowingMic()) {
                viewModel.saveMessageLocally(
                    binding.inputPanel.getEditTex().text.toString(),
                    "",
                    args.contact,
                    Constants.IsMine.YES.value
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
        }

        binding.inputPanel.setEditTextWatcher(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
                val text = binding.inputPanel.getEditTex().text.toString()

                if (text.isNotEmpty() && !isEditTextFilled) {
                    binding.inputPanel.morphFloatingActionButtonIcon()
                    isEditTextFilled = true
                    binding.inputPanel.hideImageButtonCamera()
                } else if (text.isEmpty()) {
                    binding.inputPanel.morphFloatingActionButtonIcon()
                    isEditTextFilled = false
                    binding.inputPanel.showImageButtonCamera()
                }

            }
        })

        binding.inputPanel.getImageButtonAttachment().setOnClickListener {
            val attachmentDialog = AttachmentDialogFragment()
            attachmentDialog.setListener(object :
                AttachmentDialogFragment.OnAttachmentDialogListener {
                override fun galleryPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.explanation_to_send_audio_attacment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentGalleryFoldersFragment(
                                args.contact
                            )
                        )
                    }
                }

                override fun cameraPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.CAMERA,
                        drawableIconId = R.drawable.ic_camera_primary,
                        message = R.string.explanation_camera_to_attachment_picture
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                                viewModel.getUser().id,
                                args.contact.id
                            )
                        )
                    }
                }

                override fun locationPressed() {

                }

                override fun audioPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.explanation_to_send_audio_attacment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentAudioFragment(
                                args.contact
                            )
                        )
                    }
                }

                override fun documentPressed() {

                }
            })
            attachmentDialog.show(parentFragmentManager, "attachmentDialog")
        }

        binding.inputPanel.getImageButtonCamera().setOnClickListener {
            verifyPermission(
                Manifest.permission.CAMERA,
                drawableIconId = R.drawable.ic_camera_primary,
                message = R.string.explanation_camera_to_attachment_picture
            ) {
                findNavController().navigate(
                    ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                        viewModel.getUser().id,
                        args.contact.id
                    )
                )
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shareViewModel.hasCameraSendClicked.observe(activity!!, Observer {
            if (it == true) {
                val base64 = shareViewModel.getImageBase64()

                viewModel.saveMessageWithAttachmentLocally(
                    shareViewModel.message.value!!,
                    "",
                    args.contact,
                    Constants.IsMine.YES.value,
                    base64,
                    shareViewModel.getImageUri(),
                    Constants.ATTACHMENT_ORIGIN.CAMERA.origin
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
        })

        shareViewModel.hasAudioSendClicked.observe(activity!!, Observer {
            if (it == true) {
                shareViewModel.getAudiosSelected().forEach { mediaStoreAudio ->
                    viewModel.saveMessageWithAudioAttachment(mediaStoreAudio)
                }
            }
        })

        shareViewModel.hasGallerySendClicked.observe(activity!!, Observer {
            if (it == true) {
                val base64 = shareViewModel.getImageBase64()

                viewModel.saveMessageWithAttachmentLocally(
                    shareViewModel.message.value!!,
                    "",
                    args.contact,
                    Constants.IsMine.YES.value,
                    base64,
                    shareViewModel.getImageUri(),
                    Constants.ATTACHMENT_ORIGIN.GALLERY.origin
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        viewModel.getLocalContact(args.contact.id)

        viewModel.setContact(args.contact)

        viewModel.getLocalMessages()

        setConversationBackground()

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                val snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        viewModel.messageMessages.observe(viewLifecycleOwner, Observer { conversationList ->

            if (conversationList.isNotEmpty()) {

                viewModel.sendMessagesRead()

                conversationAdapter.submitList(conversationList)

                Handler().postDelayed({
                    if (conversationAdapter.itemCount > 0) {
                        val smoothScroller: RecyclerView.SmoothScroller =
                            object : LinearSmoothScroller(context) {
                                override fun getVerticalSnapPreference(): Int {
                                    return SNAP_TO_START
                                }
                            }

                        smoothScroller.targetPosition = 0

                        linearLayoutManager.startSmoothScroll(smoothScroller)
                    }
                }, 300)
            }
        })

        viewModel.contactProfile.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                actionBarCustomView.contact = it
            }
        })
    }

    override fun onDetach() {
        (activity as MainActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        (activity as MainActivity).supportActionBar?.setDisplayShowCustomEnabled(false)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_conversation, menu)
    }

    override fun onResume() {
        super.onResume()
        mediaPlayerManager.registerProximityListener()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayerManager.pauseAudio()
        mediaPlayerManager.unregisterProximityListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        resetConversationBackground()
        mediaPlayerManager.unregisterProximityListener()
        mediaPlayerManager.resetMediaPlayer()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_see_contact -> {
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToContactProfileFragment(args.contact.id)
                )
            }
        }

        return true
    }

    private fun setConversationBackground() {
        if (viewModel.getUser().chatBackground.isNotEmpty()) {
            val uri = Uri.parse(viewModel.getUser().chatBackground)
            val inputStream: InputStream = context!!.contentResolver.openInputStream(uri)!!
            val yourDrawable = Drawable.createFromStream(inputStream, uri.toString())
            yourDrawable.alpha = (255 * 0.3).toInt()
            activity!!.window.setBackgroundDrawable(yourDrawable)
        }
    }

    private fun resetConversationBackground() {
        activity!!.window.setBackgroundDrawableResource(R.color.colorBackground)
    }

    private fun inflateCustomActionBar(inflater: LayoutInflater) {
        actionBarCustomView = DataBindingUtil.inflate(
            inflater, R.layout.conversation_action_bar, null, false
        )

        actionBarCustomView.lifecycleOwner = this

        with((activity as MainActivity).supportActionBar!!) {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setHomeButtonEnabled(false)
            setHasOptionsMenu(true)
            customView = actionBarCustomView.root
        }

        actionBarCustomView.contact = args.contact

        actionBarCustomView.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAdapter() {
        conversationAdapter = ConversationAdapter(object : ConversationAdapter.ClickListener {
            override fun clickListener(item: MessageAndAttachment) {
            }

            override fun errorPlayingAudio() {
                Utils.showSimpleSnackbar(
                    binding.coordinator,
                    getString(R.string.text_error_playing_audio),
                    3
                )
            }
        }, mediaPlayerManager)

        linearLayoutManager = LinearLayoutManager(context!!)
        linearLayoutManager.reverseLayout = true

        binding.recyclerViewConversation.apply {
            adapter = conversationAdapter
            layoutManager = linearLayoutManager

            itemAnimator =
                SlideInUpAnimator()
        }

    }

    //region Implementation MediaPlayerManager.Listener
    override fun onErrorPlayingAudio() {
        Utils.showSimpleSnackbar(
            binding.coordinator,
            getString(R.string.text_error_playing_audio),
            3
        )
    }

    //endregion
}
