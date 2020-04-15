package com.naposystems.pepito.ui.conversation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.core.graphics.toRect
import androidx.databinding.DataBindingUtil
import androidx.emoji.text.EmojiCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationActionBarBinding
import com.naposystems.pepito.databinding.ConversationFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.model.emojiKeyboard.Emoji
import com.naposystems.pepito.ui.actionMode.ActionModeMenu
import com.naposystems.pepito.ui.attachment.AttachmentDialogFragment
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.baseFragment.BaseViewModel
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversationCall.ConversationCallActivity
import com.naposystems.pepito.ui.deletionDialog.DeletionMessagesDialogFragment
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.pepito.ui.napoleonKeyboard.NapoleonKeyboard
import com.naposystems.pepito.ui.napoleonKeyboardEmojiPage.adapter.NapoleonKeyboardEmojiPageAdapter
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeViewModel
import com.naposystems.pepito.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.adapters.verifyCameraAndMicPermission
import com.naposystems.pepito.utility.adapters.verifyPermission
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.io.InputStream
import javax.inject.Inject

class ConversationFragment : BaseFragment(),
    MediaPlayerManager.Listener {

    companion object {
        fun newInstance() = ConversationFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    private val viewModel: ConversationViewModel by viewModels {
        viewModelFactory
    }
    private val selfDestructTimeViewModel: SelfDestructTimeViewModel by viewModels {
        viewModelFactory
    }

    private val shareViewModel: ConversationShareViewModel by activityViewModels()
    private val userDisplayFormatShareViewModel : UserDisplayFormatShareViewModel by activityViewModels {
        viewModelFactory
    }
    private val shareContactViewModel: ShareContactViewModel by viewModels {
        viewModelFactory
    }
    private val contactProfileShareViewModel : ContactProfileShareViewModel by activityViewModels{
        viewModelFactory
    }

    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var actionBarCustomView: ConversationActionBarBinding
    private lateinit var binding: ConversationFragmentBinding
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val args: ConversationFragmentArgs by navArgs()
    private var isEditTextFilled: Boolean = false
    private lateinit var actionMode: ActionModeMenu
    private var contactSilenced: Boolean = false
    private var menuOptionsContact: Menu? = null
    private lateinit var deletionMessagesDialog: DeletionMessagesDialogFragment

    private var clipboard: ClipboardManager? = null
    private var clipData: ClipData? = null

    private var swipeBack = false
    private val maxPositionSwipe = 3
    private val maxPositionQuoteIcon = 400
    private var leftReactF = 0f
    private var rightReactF = 0f
    private var heightItem = 0
    private var verticalCenter = 0
//    private var isVibrateTop = true


    private val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(context!!, R.anim.scale_up)
    }
    private val animationScaleDown: Animation by lazy {
        AnimationUtils.loadAnimation(context!!, R.anim.scale_down)
    }

    private val mediaPlayerManager: MediaPlayerManager by lazy {
        MediaPlayerManager(context!!)
    }

    private val emojiKeyboard by lazy {
        NapoleonKeyboard(
            binding.coordinator,
            binding.inputPanel.getEditTex(),
            object : NapoleonKeyboardEmojiPageAdapter.OnNapoleonKeyboardEmojiPageAdapterListener {
                override fun onEmojiClick(emoji: Emoji) {
                    binding.inputPanel.getEditTex().text?.append(
                        EmojiCompat.get().process(String(emoji.code, 0, emoji.code.size))
                    )
                }
            }
        )
    }


    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, RIGHT) {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return ItemTouchHelper.Callback.makeMovementFlags(0, RIGHT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            //Nothing
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //Nothing
        }

        override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
            if (swipeBack) {
                swipeBack = false
                return 0
            }
            return super.convertToAbsoluteDirection(flags, layoutDirection)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            actionSwipeQuote(actionState, recyclerView, dX, viewHolder, c)

            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX / 2,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
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

        setupActionMode()
        setupAdapter()

        inputPanelFabClickListener()

        inputPanelEditTextWatcher()

        inputPanelAttachMentButtonClickListener()

        inputPanelCameraButtonClickListener()

        binding.buttonCall.setOnClickListener {
            this.verifyCameraAndMicPermission {
                viewModel.setIsVideoCall(false)
                viewModel.callContact()
            }
        }

        binding.buttonVideoCall.setOnClickListener {
            this.verifyCameraAndMicPermission {
                viewModel.setIsVideoCall(true)
                viewModel.callContact()
            }
        }

        binding.inputPanel.getImageButtonEmoji().setOnClickListener {
            emojiKeyboard.toggle()
        }

        clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        return binding.root
    }

    private fun inputPanelCameraButtonClickListener() {
        binding.inputPanel.getImageButtonCamera().setOnClickListener {
            verifyCameraAndMicPermission {
                findNavController().navigate(
                    ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                        viewModel.getUser().id,
                        args.contact.id,
                        binding.inputPanel.getWebIdQuote(),
                        Constants.LocationImageSelectorBottomSheet.CONVERSATION.location
                    )
                )
            }
        }
    }

    private fun inputPanelAttachMentButtonClickListener() {
        binding.inputPanel.getImageButtonAttachment().setOnClickListener {
            validateStateOutputControl()
            val attachmentDialog = AttachmentDialogFragment()
            attachmentDialog.setListener(object :
                AttachmentDialogFragment.OnAttachmentDialogListener {
                override fun galleryPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.text_explanation_to_send_audio_attacment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentGalleryFoldersFragment(
                                args.contact,
                                binding.inputPanel.getWebIdQuote(),
                                Constants.LocationImageSelectorBottomSheet.CONVERSATION.location
                            )
                        )
                    }
                }

                override fun cameraPressed() {
                    this@ConversationFragment.verifyCameraAndMicPermission {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                                viewModel.getUser().id,
                                args.contact.id,
                                binding.inputPanel.getWebIdQuote(),
                                Constants.LocationImageSelectorBottomSheet.CONVERSATION.location
                            )
                        )
                    }
                }

                override fun locationPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        drawableIconId = R.drawable.ic_location_on_primary,
                        message = R.string.text_explanation_to_send_location_attachment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentLocationFragment()
                        )
                    }
                }

                override fun audioPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.text_explanation_to_send_audio_attacment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentAudioFragment(
                                args.contact
                            )
                        )
                    }
                }

                override fun documentPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.text_explanation_to_send_audio_attacment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentDocumentFragment()
                        )
                    }
                }
            })
            attachmentDialog.show(parentFragmentManager, "attachmentDialog")
        }
    }

    private fun inputPanelEditTextWatcher() {
        binding.inputPanel.setEditTextWatcher(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputPanel.apply {
                    val text = getEditTex().text.toString()

                    if (text.isNotEmpty() && !isEditTextFilled) {
                        morphFloatingActionButtonIcon()
                        isEditTextFilled = true
                        hideImageButtonCamera()
                    } else if (text.isEmpty()) {
                        morphFloatingActionButtonIcon()
                        isEditTextFilled = false
                        showImageButtonCamera()
                    }
                }
            }
        })
    }

    private fun inputPanelFabClickListener() {
        binding.inputPanel.getFloatingActionButton().setOnClickListener {
            if (!binding.inputPanel.getFloatingActionButton().isShowingMic()) {

                val quote = binding.inputPanel.getQuote()

                viewModel.saveMessageLocally(
                    binding.inputPanel.getEditTex().text.toString(),
                    obtainTimeSelfDestruct(),
                    quote?.message?.webId ?: ""
                )

                with(binding.inputPanel.getEditTex()) {
                    setText("")
                }
            }
            handlerGoDown()
            binding.inputPanel.closeQuote()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        shareViewModel.hasAudioSendClicked.observe(activity!!, Observer {
            if (it == true) {
                shareViewModel.getAudiosSelected().forEach { mediaStoreAudio ->
                    viewModel.saveMessageWithAudioAttachment(
                        mediaStoreAudio,
                        obtainTimeSelfDestruct(),
                        binding.inputPanel.getWebIdQuote()
                    )
                }
            }
        })

        shareViewModel.attachmentSelected.observe(activity!!, Observer { attachment ->
            if (attachment != null) {
                viewModel.saveMessageAndAttachment(
                    shareViewModel.getMessage() ?: "",
                    attachment,
                    1,
                    obtainTimeSelfDestruct(),
                    shareViewModel.getQuoteWebId() ?: ""
                )
            }
        })

        shareViewModel.gifSelected.observe(activity!!, Observer { gifAttachment ->
            if (gifAttachment != null) {
                findNavController().navigate(
                    ConversationFragmentDirections.actionConversationFragmentToAttachmentPreviewFragment(
                        gifAttachment,
                        0,
                        shareViewModel.getQuoteWebId() ?: ""
                    )
                )
                shareViewModel.resetGifSelected()
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.viewModel = viewModel

        contactProfileShareViewModel.getLocalContact(args.contact.id)

        viewModel.setContact(args.contact)

        viewModel.getLocalMessages()

        viewModel.getMessagesSelected(args.contact.id)

        selfDestructTimeViewModel.getSelfDestructTimeByContact(args.contact.id)

        selfDestructTimeViewModel.getSelfDestructTime()

        baseViewModel.getOutputControl()

        setConversationBackground()

        selfDestructTimeViewModel.getDestructTimeByContact.observe(viewLifecycleOwner, Observer {
            selfDestructTimeViewModel.selfDestructTimeByContact = it
        })

        observeMessagesSelected()

        observeWebServiceError()

        observeDeleteMessagesForAllWsError()

        observeMessageMessages()

        observeContactProfile()

        observeStringsCopy()

        observeResponseDeleteLocalMessages()

        viewModel.contactCalledSuccessfully.observe(viewLifecycleOwner, Observer { channel ->
            if (!channel.isNullOrEmpty()) {
                /*findNavController().navigate(
                    ConversationFragmentDirections.actionConversationFragmentToConversationCallFragment(
                        contact = args.contact,
                        channel = channel,
                        isIncomingCall = false,
                        isVideoCall = viewModel.isVideoCall()
                    )
                )*/
                val intent = Intent(context, ConversationCallActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putInt(ConversationCallActivity.CONTACT_ID, args.contact.id)
                        putString(ConversationCallActivity.CHANNEL, channel)
                        putBoolean(ConversationCallActivity.IS_VIDEO_CALL, viewModel.isVideoCall())
                        putBoolean(ConversationCallActivity.IS_INCOMING_CALL, false)
                    })
                }
                startActivity(intent)
                (context as MainActivity).overridePendingTransition(
                    R.anim.slide_in_up,
                    R.anim.slide_out_down
                )
                viewModel.resetContactCalledSuccessfully()
                viewModel.resetIsVideoCall()
            }
        })
    }

    private fun observeResponseDeleteLocalMessages() {
        viewModel.responseDeleteLocalMessages.observe(viewLifecycleOwner, Observer {
            if (it && actionMode.mode != null) {
                actionMode.mode!!.finish()
            }
        })
    }

    private fun observeStringsCopy() {
        viewModel.stringsCopy.observe(viewLifecycleOwner, Observer {
            if (it.count() == 1) {
                copyDataInClipboard(viewModel.parsingListByTextBlock(it))
                viewModel.resetListStringCopy()
                Toast.makeText(context, R.string.text_message_copied, Toast.LENGTH_LONG).show()
                actionMode.mode!!.finish()
            }
        })
    }

    private fun observeContactProfile() {
        contactProfileShareViewModel.contact.observe(viewLifecycleOwner, Observer { contact ->
            if (contact != null) {
                actionBarCustomView.contact = contact
                setTextSilenceOfMenu(contact)
            }
        })
    }

    private fun observeMessageMessages() {
        viewModel.messageMessages.observe(viewLifecycleOwner, Observer { conversationList ->
            conversationAdapter.submitList(conversationList)

            if (conversationList.isNotEmpty()) {
                viewModel.sendMessagesRead()
            }

        })
    }

    private fun observeDeleteMessagesForAllWsError() {
        viewModel.deleteMessagesForAllWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showSnackbar(it)
            }
        })
    }

    private fun observeWebServiceError() {
        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showSnackbar(it)
            }
        })
    }

    private fun observeMessagesSelected() {
        viewModel.messagesSelected.observe(
            viewLifecycleOwner, Observer { listMessageAndAttachment ->

                val quantityMessagesOtherUser = listMessageAndAttachment.filter {
                    it.message.isMine == 0
                }.toList().count()

                val quantityMessagesFailed = listMessageAndAttachment.filter {
                    it.message.status == Constants.MessageStatus.ERROR.status
                }.toList().count()

                actionMode.hideCopyButton = false

                setupWidgets(0, View.GONE)

                when (listMessageAndAttachment.count()) {

                    Constants.QUANTITY_TO_HIDE_ACTIONMODE -> {
                        actionMode.mode?.finish()
                        setupWidgets(binding.containerStatus.height, View.VISIBLE)
                    }

                    Constants.QUANTITY_MIN_TO_SHOW_ACTIONMODE -> {
                        listMessageAndAttachment.forEach { messageAndAttachment ->
                            if (messageAndAttachment.attachmentList.count() > Constants.QUANTITY_ATTACHMENTS) {
                                actionMode.hideCopyButton = true
                            }
                        }
                        actionMode.quantityMessageOtherUser = quantityMessagesOtherUser
                        actionMode.quantityMessagesFailed = quantityMessagesFailed
                    }

                    else -> {
                        actionMode.hideCopyButton = true
                        actionMode.quantityMessageOtherUser = quantityMessagesOtherUser
                        actionMode.quantityMessagesFailed = quantityMessagesFailed
                    }
                }
                actionMode.mode?.invalidate()
                actionMode.changeTitle(listMessageAndAttachment.count().toString())
            })
    }

    private fun setupWidgets(sizePaddingTop: Int, visible: Int) {
        binding.containerStatus.visibility = visible
        binding.recyclerViewConversation.setPadding(0, sizePaddingTop, 0, 0)
    }

    private fun showSnackbar(listError: List<String>) {
        val snackbarUtils = SnackbarUtils(binding.coordinator, listError)
        snackbarUtils.showSnackbar()
    }

    private fun handlerGoDown() {
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

    override fun onDetach() {
        (activity as MainActivity).supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        (activity as MainActivity).supportActionBar?.setDisplayShowCustomEnabled(false)
        super.onDetach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuOptionsContact = menu
        inflater.inflate(R.menu.menu_conversation, menu)
        contactProfileShareViewModel.contact.value?.let { contact ->
            setTextSilenceOfMenu(contact)
        }
    }

    private fun setTextSilenceOfMenu(contact: Contact) {
        menuOptionsContact?.let { menuOptions ->
            if (contact.silenced) {
                menuOptions.findItem(R.id.menu_item_mute_conversation).title =
                    context?.getString(R.string.text_unmuted_conversation)
            } else {
                menuOptions.findItem(R.id.menu_item_mute_conversation).title =
                    context?.getString(R.string.text_mute_conversation)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mediaPlayerManager.registerProximityListener()
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
            R.id.menu_item_schedule -> {
                val dialog = SelfDestructTimeDialogFragment.newInstance(args.contact.id)
                dialog.setListener(object :
                    SelfDestructTimeDialogFragment.SelfDestructTimeListener {
                    override fun onSelfDestructTimeChange(selfDestructTimeSelected: Int) {
                        selfDestructTimeViewModel.setSelfDestructTimeByContact(
                            selfDestructTimeSelected,
                            args.contact.id
                        )
                    }
                })
                dialog.show(childFragmentManager, "SelfDestructTime")
            }
            R.id.menu_item_block_contact -> {
                blockContact(args.contact)
            }
            R.id.menu_item_mute_conversation -> {
                contactProfileShareViewModel.contact.value?.let { contact ->
                    if (contact.silenced)
                        desactiveSilence()
                    else
                        silenceConversation()
                }
            }
            R.id.menu_item_delete_messages -> {
                optionDeleteMessagesClickListener()
            }
        }

        return true
    }

    private fun blockContact(contact: Contact) {
        generalDialog(
            getString(R.string.text_block_contact),
            getString(
                R.string.text_wish_block_contact,
                if (contact.displayNameFake.isEmpty()) {
                    contact.displayName
                } else {
                    contact.displayNameFake
                }
            ),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.sendBlockedContact(args.contact)
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun silenceConversation() {
        contactProfileShareViewModel.contact.value?.let { contact ->
            val dialog = MuteConversationDialogFragment.newInstance(
                args.contact.id, contact.silenced
            )
            dialog.setListener(object : MuteConversationDialogFragment.MuteConversationListener {
                override fun onMuteConversationChange() {
                    // Intentionally empty
                }
            })
            dialog.show(childFragmentManager, "MuteConversation")
        }
    }

    private fun desactiveSilence() {
        contactProfileShareViewModel.contact.value?.let { contact ->
            shareContactViewModel.muteConversation(args.contact.id, contact.silenced)
        }
    }

    private fun deleteConversation() {
        generalDialog(
            getString(R.string.text_title_delete_conversation),
            getString(R.string.text_want_delete_conversation),
            true,
            childFragmentManager
        ) {
            shareContactViewModel.deleteConversation(args.contact.id)
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun copyDataInClipboard(text: String) {
        clipData = ClipData.newPlainText("text", text)
        clipboard?.setPrimaryClip(clipData!!)
    }

    private fun setConversationBackground() {
        val chatBackgroundFileName = viewModel.getUser().chatBackground
        activity?.let { activity ->
            context?.let { context ->
                if (chatBackgroundFileName.isNotEmpty()) {
                    val uri = Utils.getFileUri(
                        context = context,
                        fileName = chatBackgroundFileName,
                        subFolder = Constants.NapoleonCacheDirectories.CHAT_BACKGROUND.folder
                    )
                    val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                    val backgroundDrawable = Drawable.createFromStream(inputStream, uri.toString())
                    backgroundDrawable.alpha = (255 * 0.3).toInt()
                    activity.window.setBackgroundDrawable(backgroundDrawable)
                } else {
                    activity.window.setBackgroundDrawableResource(R.drawable.vertical_photo)
                }
            }
        }
    }

    private fun obtainTimeSelfDestruct(): Int {
        return if (selfDestructTimeViewModel.selfDestructTimeByContact!! < 0) {
            selfDestructTimeViewModel.selfDestructTimeGlobal.value!!
        } else {
            selfDestructTimeViewModel.selfDestructTimeByContact!!
        }
    }

    private fun resetConversationBackground() {
        val value = TypedValue()
        context!!.theme.resolveAttribute(R.attr.attrBackgroundColorBackground, value, true)
        activity!!.window.setBackgroundDrawableResource(value.resourceId)
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

        actionBarCustomView.viewModel = userDisplayFormatShareViewModel

        actionBarCustomView.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        actionBarCustomView.containerDescriptionUser.setOnClickListener {
            findNavController().navigate(
                ConversationFragmentDirections
                    .actionConversationFragmentToContactProfileFragment(args.contact.id)
            )
        }
    }

    private fun setupActionMode() {
        actionMode = ActionModeMenu(
            clickCopy = {
                viewModel.copyMessagesSelected(args.contact.id)
            },
            clickDelete = { moreMessagesOtherContact ->
                if (moreMessagesOtherContact) {
                    dialogWithoutNeutralButton(Constants.DeleteMessages.BY_SELECTION.option)
                } else {
                    dialogWithNeutralButton(Constants.DeleteMessages.BY_SELECTION.option)
                }
            }, clickBack = {
                cleanSelectionMessages()
            })
    }

    private fun dialogWithNeutralButton(status: Int) {
        viewModel.messagesSelected.value?.let { messagesSelected ->
            Utils.alertDialogWithNeutralButton(
                R.string.text_delete_messages,
                false, context!!,
                R.string.text_delete_message_for_me,
                R.string.text_cancel,
                R.string.text_delete_message_for_all,
                clickTopButton = { _ ->
                    when (status) {
                        Constants.DeleteMessages.BY_SELECTION.option -> {
                            viewModel.deleteMessagesSelected(args.contact.id, messagesSelected)
                        }
                        Constants.DeleteMessages.BY_UNREADS.option -> {
                            viewModel.deleteMessagesByStatusForMe(args.contact.id, status)
                        }
                        Constants.DeleteMessages.BY_UNRECEIVED.option -> {
                            viewModel.deleteMessagesByStatusForMe(args.contact.id, status)
                        }
                    }
                },
                clickDownButton = { _ ->
                    when (status) {
                        Constants.DeleteMessages.BY_SELECTION.option -> {
                            viewModel.deleteMessagesForAll(args.contact.id, messagesSelected)
                        }
                        Constants.DeleteMessages.BY_UNREADS.option -> {
                            viewModel.deleteMessagesByStatusForAll(args.contact.id, status)
                        }
                        Constants.DeleteMessages.BY_UNRECEIVED.option -> {
                            viewModel.deleteMessagesByStatusForAll(args.contact.id, status)
                        }
                    }
                }
            )
        }
    }

    private fun dialogWithoutNeutralButton(status: Int) {
        viewModel.messagesSelected.value?.let { listMessagesAndAttachments ->
            Utils.alertDialogWithoutNeutralButton(
                R.string.text_delete_messages,
                false, context!!,
                R.string.text_accept,
                R.string.text_cancel,
                clickPositiveButton = { _ ->
                    when (status) {
                        Constants.DeleteMessages.BY_SELECTION.option -> {
                            viewModel.deleteMessagesSelected(
                                args.contact.id,
                                listMessagesAndAttachments
                            )
                        }
                        Constants.DeleteMessages.BY_FAILED.option -> {
                            viewModel.deleteMessagesByStatusForMe(args.contact.id, status)
                        }
                    }
                }
            )
        }
    }

    private fun setupAdapter() {
        conversationAdapter = ConversationAdapter(object : ConversationAdapter.ClickListener {
            override fun onClick(item: MessageAndAttachment) {
                conversationAdapterOnClickEvent(item)
            }

            override fun onLongClick(item: Message) {
                if (actionMode.mode == null) {
                    actionMode.startActionMode(view, R.menu.menu_selection_message)
                    updateStateSelectionMessage(item)
                }
            }

            override fun messageToEliminate(item: MessageAndAttachment) {
                val messages = arrayListOf<MessageAndAttachment>()
                messages.add(item)
                viewModel.deleteMessagesSelected(args.contact.id, messages)
            }

            override fun errorPlayingAudio() {
                Utils.showSimpleSnackbar(
                    binding.coordinator,
                    getString(R.string.text_error_playing_audio),
                    3
                )
            }

            override fun onPreviewClick(item: MessageAndAttachment) {
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToPreviewMediaFragment(item)
                )
            }

            override fun goToQuote(messageAndAttachment: MessageAndAttachment) {
                val position = viewModel.getMessagePosition(messageAndAttachment)

                if (position != -1) {
                    binding.recyclerViewConversation.smoothScrollToPosition(position)
                } else {
                    Toast.makeText(
                        context, "No se encuentra el mensaje original|!!", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, mediaPlayerManager)

        linearLayoutManager = LinearLayoutManager(context!!)
        linearLayoutManager.reverseLayout = true

        binding.recyclerViewConversation.adapter = conversationAdapter
        binding.recyclerViewConversation.layoutManager = linearLayoutManager

        binding.recyclerViewConversation.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    linearLayoutManager.findFirstVisibleItemPosition() <= Constants.QUANTITY_TO_SHOW_FAB_CONVERSATION -> {
                        if (binding.textViewNotificationMessage.visibility == View.VISIBLE) {
                            showFabScroll(View.INVISIBLE, animationScaleDown)
                        }
                    }
                    else -> {
                        if (binding.textViewNotificationMessage.visibility != View.VISIBLE) {
                            showFabScroll(View.VISIBLE, animationScaleUp)
                        }
                        binding.fabGoDown.setOnClickListener {
                            handlerGoDown()
                        }
                    }
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewConversation)
    }

    private fun conversationAdapterOnClickEvent(item: MessageAndAttachment) {
        if (actionMode.mode != null) {
            updateStateSelectionMessage(item.message)
        }

        if (item.attachmentList.isNotEmpty()) {
            val firstAttachment = item.attachmentList.first()

            if (firstAttachment.type == Constants.AttachmentType.DOCUMENT.type) {
                openAttachmentDocument(firstAttachment)
            }
        }
    }

    private fun showFabScroll(visible: Int, animation: Animation) {
        binding.fabGoDown.startAnimation(animation)
        binding.textViewNotificationMessage.startAnimation(animation)
        binding.fabGoDown.visibility = visible
        binding.textViewNotificationMessage.visibility = visible
    }

    private fun updateStateSelectionMessage(item: Message) {
        viewModel.updateStateSelectionMessage(args.contact.id, item.id, item.isSelected)
    }

    private fun cleanSelectionMessages() {
        viewModel.cleanSelectionMessages(args.contact.id)
    }

    private fun openAttachmentDocument(attachment: Attachment) {
        val intent = Intent(Intent.ACTION_VIEW)

        val uri: Uri = Utils.getFileUri(
            context!!,
            attachment.uri,
            Constants.NapoleonCacheDirectories.DOCUMENTOS.folder
        )
        val extension = MimeTypeMap.getFileExtensionFromUrl(
            uri.toString()
        )
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        if (extension.equals("", ignoreCase = true) || mimeType == null) {
            // if there is no extension or there is no definite mimetype, still try to open the file
            intent.setDataAndType(uri, "text/*")
        } else {
            intent.setDataAndType(uri, mimeType)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // custom message for the intent
        startActivity(Intent.createChooser(intent, "Choose an Application:"))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun actionSwipeQuote(
        actionState: Int,
        recyclerView: RecyclerView,
        dX: Float,
        viewHolder: RecyclerView.ViewHolder,
        c: Canvas
    ) {
        val icon = resources.getDrawable(R.drawable.ic_quote_new, null)
        /*val maxWidthSwipe = recyclerView.width / 3
            if (dX > maxWidthSwipe.toFloat() && isVibrateTop) {
                Utils.vibratePhone(context, Constants.Vibrate.SOFT.type)
                isVibrateTop = false
            }*/

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            recyclerView.setOnTouchListener { _, event ->
                swipeBack = event?.action == MotionEvent.ACTION_CANCEL ||
                        event?.action == MotionEvent.ACTION_UP
                if (swipeBack && dX > recyclerView.width / maxPositionSwipe) {
                    val position = viewHolder.adapterPosition
                    binding.inputPanel.resetImage()
                    conversationAdapter
                        .getMessageAndAttachment(position)?.let { messageAndAttachment ->
                            if (messageAndAttachment.message.status == Constants.MessageStatus.ERROR.status) {
                                this.showToast("No se puede citar de un mensaje fallido|!!")
                            } else {
                                binding.inputPanel.openQuote(messageAndAttachment)
                            }
                        }
                }
                false
            }
        }

        c.clipRect(
            0f, viewHolder.itemView.top.toFloat(),
            viewHolder.itemView.right.toFloat(), viewHolder.itemView.bottom.toFloat()
        )

        heightItem = viewHolder.itemView.bottom - viewHolder.itemView.top
        verticalCenter =
            (viewHolder.itemView.top + (heightItem / 2)) - (icon.intrinsicHeight / 2)

        if (dX < maxPositionQuoteIcon) {
            rightReactF = dX / maxPositionSwipe
            leftReactF = rightReactF - icon.intrinsicWidth
        }

        icon.bounds = RectF(
            leftReactF,
            verticalCenter.toFloat(),
            rightReactF,
            (verticalCenter + icon.intrinsicHeight).toFloat()
        ).toRect()

        icon.draw(c)
    }

    override fun onPause() {
        super.onPause()
        resetConversationBackground()
        mediaPlayerManager.pauseAudio()
        mediaPlayerManager.unregisterProximityListener()
        if (actionMode.mode != null) {
            actionMode.mode!!.finish()
        }
    }

    private fun optionDeleteMessagesClickListener() {
        deletionMessagesDialog = DeletionMessagesDialogFragment(clickDeleteConversation = {
            deletionMessagesDialog.dismiss()
            deleteConversation()
        }, clickDeleteUnreads = {
            deletionMessagesDialog.dismiss()
            dialogWithNeutralButton(Constants.DeleteMessages.BY_UNREADS.option)
        }, clickDeleteUnreceived = {
            deletionMessagesDialog.dismiss()
            dialogWithNeutralButton(Constants.DeleteMessages.BY_UNRECEIVED.option)
        }, clickDeleteFiled = {
            deletionMessagesDialog.dismiss()
            dialogWithoutNeutralButton(Constants.DeleteMessages.BY_FAILED.option)
        })
        deletionMessagesDialog.show(childFragmentManager, "DeletionMessages")
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
