package com.naposystems.pepito.ui.conversation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.media.MediaRecorder
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
import androidx.constraintlayout.widget.ConstraintSet
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
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationActionBarBinding
import com.naposystems.pepito.databinding.ConversationFragmentBinding
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.reactive.RxBus
import com.naposystems.pepito.reactive.RxEvent
import com.naposystems.pepito.ui.actionMode.ActionModeMenu
import com.naposystems.pepito.ui.attachment.AttachmentDialogFragment
import com.naposystems.pepito.ui.baseFragment.BaseFragment
import com.naposystems.pepito.ui.baseFragment.BaseViewModel
import com.naposystems.pepito.ui.conversation.adapter.ConversationAdapter
import com.naposystems.pepito.ui.conversationCall.ConversationCallActivity
import com.naposystems.pepito.ui.custom.fabSend.FabSend
import com.naposystems.pepito.ui.deletionDialog.DeletionMessagesDialogFragment
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.ui.muteConversation.MuteConversationDialogFragment
import com.naposystems.pepito.ui.napoleonKeyboard.NapoleonKeyboard
import com.naposystems.pepito.ui.selfDestructTime.Location
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeViewModel
import com.naposystems.pepito.utility.*
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.adapters.slideUp
import com.naposystems.pepito.utility.adapters.verifyCameraAndMicPermission
import com.naposystems.pepito.utility.adapters.verifyPermission
import com.naposystems.pepito.utility.mediaPlayer.MediaPlayerManager
import com.naposystems.pepito.utility.sharedViewModels.contact.ShareContactViewModel
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.timeFormat.TimeFormatShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.userDisplayFormat.UserDisplayFormatShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationFragment : BaseFragment(),
    MediaPlayerManager.Listener, FabSend.FabSendListener {

    companion object {
        const val RC_DOCUMENT = 2511
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
    private val userDisplayFormatShareViewModel: UserDisplayFormatShareViewModel by activityViewModels {
        viewModelFactory
    }
    private val shareContactViewModel: ShareContactViewModel by viewModels {
        viewModelFactory
    }
    private val contactProfileShareViewModel: ContactProfileShareViewModel by activityViewModels {
        viewModelFactory
    }
    private val timeFormatShareViewModel: TimeFormatShareViewModel by activityViewModels {
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
    private var menuOptionsContact: Menu? = null
    private lateinit var deletionMessagesDialog: DeletionMessagesDialogFragment

    private var clipboard: ClipboardManager? = null
    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var clipData: ClipData? = null
    private var mRecordingAudioRunnable: Runnable? = null

    private var keyboardHeight: Int = 0
    private var recordingTime: Long = 0
    private var swipeBack = false
    private val maxPositionSwipe = 3
    private val maxPositionQuoteIcon = 400
    private var leftReactF = 0f
    private var rightReactF = 0f
    private var heightItem = 0
    private var verticalCenter = 0
    private var isRecordingAudio: Boolean = false
    private var minTimeRecording = TimeUnit.SECONDS.toMillis(1)
    private var messagedLoadedFirstTime: Boolean = false

    private val mHandler: Handler by lazy {
        Handler()
    }

    private val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
    }
    private val animationScaleDown: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)
    }

    private val mediaPlayerManager: MediaPlayerManager by lazy {
        MediaPlayerManager(requireContext())
    }

    private var emojiKeyboard: NapoleonKeyboard? = null

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

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        subscribeRxEvents()

        inflateCustomActionBar(inflater)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_fragment, container, false
        )

        emojiKeyboard = NapoleonKeyboard(
            binding.coordinator,
            binding.inputPanel.getEditTex()
        )

        binding.lifecycleOwner = this

        binding.contact = args.contact

        binding.floatingActionButtonSend.setListener(this)

        setupActionMode()

        setupAdapter()

        inputPanelFabClickListener()

        inputPanelEditTextWatcher()

        inputPanelAttachMentButtonClickListener()

        inputPanelCameraButtonClickListener()

        binding.inputPanel.getTextCancelAudio().setOnClickListener {
            isRecordingAudio = false
            resetAudioRecording()
        }

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
            emojiKeyboard?.toggle(keyboardHeight)
        }

        binding.fabGoDown.setOnClickListener {
            handlerGoDown()
        }

        binding.recyclerViewConversation.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val friendlyMessageCount: Int = conversationAdapter.itemCount - 1
                val lastVisiblePosition: Int =
                    linearLayoutManager.findLastVisibleItemPosition()

                val invisibleItems = friendlyMessageCount - lastVisiblePosition
//                Timber.d("invisibleItems: $invisibleItems")

                if (invisibleItems >= Constants.QUANTITY_TO_SHOW_FAB_CONVERSATION) {
                    showFabScroll(View.VISIBLE, animationScaleUp)
                } else {
                    showFabScroll(View.GONE, animationScaleDown)
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        binding.recyclerViewConversation.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.recyclerViewConversation.post {
                    val friendlyMessageCount: Int = conversationAdapter.itemCount
                    binding.recyclerViewConversation.scrollToPosition(friendlyMessageCount - 1)
                }
            }
        }

        clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        return binding.root
    }

    private fun subscribeRxEvents() {
        val disposableEmojiSelected = RxBus.listen(RxEvent.EmojiSelected::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.inputPanel.getEditTex().text?.append(
                    EmojiCompat.get().process(String(it.emoji.code, 0, it.emoji.code.size))
                )
            }

        disposable.add(disposableEmojiSelected)
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
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "*/*"
                        intent.putExtra(
                            Intent.EXTRA_MIME_TYPES, arrayOf(
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-powerpoint",
                                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                            )
                        )
                        startActivityForResult(intent, RC_DOCUMENT)
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
                        binding.floatingActionButtonSend.morphToSend()
                        isEditTextFilled = true
                        hideImageButtonCamera()
                    } else if (text.isEmpty()) {
                        binding.floatingActionButtonSend.morphToMic()
                        isEditTextFilled = false
                        showImageButtonCamera()
                    }
                }
            }
        })
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun inputPanelFabClickListener() {
        with(binding.floatingActionButtonSend) {
            this.setOnClickListener {
                Timber.d("setOnClickListener")
                if (!this.isShowingMic() && !isRecordingAudio) {
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

                if (!this.isShowingMic() && isRecordingAudio && isLocked() && recordingTime >= minTimeRecording) {
                    saveAndSendRecordAudio()
                }

                if (this.isShowingMic() && isRecordingAudio && recordingTime >= minTimeRecording) {
                    saveAndSendRecordAudio()
                }
                binding.inputPanel.closeQuote()
            }
        }
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (emojiKeyboard?.isShowing() == true) {
                emojiKeyboard?.handleBackButton()
            } else {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }

        shareViewModel.hasAudioSendClicked.observe(requireActivity(), Observer {
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

        shareViewModel.attachmentSelected.observe(requireActivity(), Observer { attachment ->
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

        shareViewModel.gifSelected.observe(requireActivity(), Observer { gifAttachment ->
            try {
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
            } catch (e: Exception) {
                Timber.e(e)
            }
        })
    }

    @InternalCoroutinesApi
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

        timeFormatShareViewModel.getTimeFormat()

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

        viewModel.downloadAttachmentProgress.observe(viewLifecycleOwner, Observer {
            when (it) {
                is DownloadAttachmentResult.Start -> {
                    conversationAdapter.setStartDownload(it.itemPosition, it.job)
                }
                is DownloadAttachmentResult.Success -> {
                    it.messageAndAttachment.getFirstAttachment()?.let { firstAttachment ->
                        if (firstAttachment.type != Constants.AttachmentType.AUDIO.type) {
                            val message = it.messageAndAttachment.message
                            viewModel.sendMessageRead(message)
                        }

                        firstAttachment.status =
                            Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status
                        viewModel.updateAttachment(
                            firstAttachment
                        )
                    }
                }
                is DownloadAttachmentResult.Progress -> {
                    conversationAdapter.setProgress(
                        it.itemPosition,
                        it.progress
                    )
                }
                is DownloadAttachmentResult.Error -> {
                    it.attachment.status =
                        Constants.AttachmentStatus.DOWNLOAD_ERROR.status
                    viewModel.updateAttachment(
                        it.attachment
                    )
                    Timber.d("Error")
                }
            }
        })

        viewModel.uploadProgress.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                when (it) {
                    is UploadResult.Start -> conversationAdapter.setUploadStart(
                        it.attachment,
                        it.job
                    )
                    is UploadResult.Success -> conversationAdapter.setUploadComplete(it.attachment)
                    is UploadResult.Progress -> conversationAdapter.setUploadProgress(
                        it.attachment,
                        it.progress,
                        it.job
                    )
                    is UploadResult.Cancel -> {
                        val attachment: Attachment = it.attachment
                        val message: Message = it.message
                        message.status = Constants.MessageStatus.SENDING.status
                        viewModel.updateMessage(message)
                        attachment.status = Constants.AttachmentStatus.UPLOAD_CANCEL.status
                        viewModel.updateAttachment(attachment)
                        viewModel.resetUploadProgress()
                    }
                }
            }
        })

        viewModel.documentCopied.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val attachment = Attachment(
                    id = 0,
                    messageId = 0,
                    webId = "",
                    messageWebId = "",
                    type = Constants.AttachmentType.DOCUMENT.type,
                    body = "",
                    uri = it.name,
                    origin = Constants.AttachmentOrigin.GALLERY.origin,
                    thumbnailUri = "",
                    status = Constants.AttachmentStatus.SENDING.status,
                    extension = it.extension
                )

                viewModel.saveMessageAndAttachment(
                    messageString = "",
                    attachment = attachment,
                    numberAttachments = 1,
                    selfDestructTime = obtainTimeSelfDestruct(),
                    quote = ""
                )
                viewModel.resetDocumentCopied()
            }
        })
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun saveAndSendRecordAudio() {
        recordFile?.let { file ->

            recordingTime = 0L
            isRecordingAudio = false
            stopRecording()

            val attachment = Attachment(
                id = 0,
                messageId = 0,
                webId = "",
                messageWebId = "",
                type = Constants.AttachmentType.AUDIO.type,
                body = "",
                uri = file.name,
                origin = Constants.AttachmentOrigin.RECORD_AUDIO.origin,
                thumbnailUri = "",
                status = Constants.AttachmentStatus.SENDING.status,
                extension = "aac"
            )

            viewModel.saveMessageAndAttachment(
                "",
                attachment,
                1,
                obtainTimeSelfDestruct(),
                shareViewModel.getQuoteWebId() ?: ""
            )

            resetAudioRecording()
        }
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
            Timber.d("observeMessageMessages")

            conversationAdapter.submitList(conversationList)

            if (!messagedLoadedFirstTime) {
                val friendlyMessageCount: Int = conversationAdapter.itemCount
                binding.recyclerViewConversation.scrollToPosition(friendlyMessageCount - 1)
                messagedLoadedFirstTime = true
            }

            if (conversationList.isNotEmpty()) {
                viewModel.sendTextMessagesRead()
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
        val friendlyMessageCount: Int = conversationAdapter.itemCount
        binding.recyclerViewConversation.smoothScrollToPosition(friendlyMessageCount - 1)
    }

    override fun onDetach() {
        with(activity as MainActivity) {
            supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
            supportActionBar?.setDisplayShowCustomEnabled(false)

        }
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
        Timber.d("onResume")
        mediaPlayerManager.registerProximityListener()
//        setConversationBackground()
        binding.floatingActionButtonSend.morphToMic()
        messagedLoadedFirstTime = false
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        resetConversationBackground()
        mediaPlayerManager.unregisterProximityListener()
        mediaPlayerManager.resetMediaPlayer()
        emojiKeyboard?.dispose()
        disposable.dispose()
        if (mRecordingAudioRunnable != null) {
            mHandler.removeCallbacks(mRecordingAudioRunnable!!)
            mRecordingAudioRunnable = null
        }
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
                val dialog = SelfDestructTimeDialogFragment.newInstance(
                    args.contact.id,
                    Location.CONVERSATION
                )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != RC_DOCUMENT || resultCode != RESULT_OK)
            return

        Timber.d("URI FILE: ${data?.data.toString()}")
        data?.data?.let { uri ->
            viewModel.sendDocumentAttachment(uri)
        }
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
                    activity.window.decorView.background = resources.getDrawable(
                        R.drawable.bg_default_conversation,
                        requireContext().theme
                    )
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
        requireContext().theme.resolveAttribute(R.attr.attrBackgroundColorBackground, value, true)
        requireActivity().window.setBackgroundDrawableResource(value.resourceId)
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
                false, requireContext(),
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
                false, requireContext(),
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

    @InternalCoroutinesApi
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

            override fun downloadAttachment(
                messageAndAttachment: MessageAndAttachment,
                itemPosition: Int?
            ) {
                Timber.d("downloadAttachment")
                if (itemPosition != null && messageAndAttachment.getFirstAttachment() != null) {
                    viewModel.downloadAttachment(messageAndAttachment, itemPosition)
                }
            }

            override fun uploadAttachment(attachment: Attachment, message: Message) {
                viewModel.uploadAttachment(attachment, message)
            }

            override fun updateAttachmentState(attachment: Attachment) {
                viewModel.updateAttachment(attachment)
            }
        }, mediaPlayerManager, timeFormatShareViewModel.getValTimeFormat())

        linearLayoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewConversation.setHasFixedSize(false)
        binding.recyclerViewConversation.adapter = conversationAdapter
        binding.recyclerViewConversation.layoutManager = linearLayoutManager
        binding.recyclerViewConversation.itemAnimator = ItemAnimator()

        conversationAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (messagedLoadedFirstTime) {
                    val friendlyMessageCount: Int = conversationAdapter.itemCount
                    val lastVisiblePosition: Int =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition()
//                    Timber.d("friendlyMessageCount: $friendlyMessageCount, lastVisiblePosition: $lastVisiblePosition, positionStart: $positionStart, itemCount: $itemCount")
                    if (lastVisiblePosition == -1 ||
                        positionStart >= friendlyMessageCount - 1 &&
                        lastVisiblePosition == positionStart - 1
                    ) {
                        binding.recyclerViewConversation.scrollToPosition(positionStart)
                    }
                }
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

            if (firstAttachment.type == Constants.AttachmentType.DOCUMENT.type &&
                firstAttachment.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status
            ) {
                openAttachmentDocument(firstAttachment)
            }
        }
    }

    private fun showFabScroll(visible: Int, animation: Animation) {
        if (visible != binding.fabGoDown.visibility) {
            binding.fabGoDown.visibility = visible
            binding.fabGoDown.startAnimation(animation)
//        binding.textViewNotificationMessage.startAnimation(animation)
//        binding.textViewNotificationMessage.visibility = visible
        }
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
            requireContext(),
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
        Timber.d("onPause")
        resetAudioRecording()
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

    private fun resetAudioRecording() {
        binding.inputPanel.changeViewSwitcherToInputPanel()
        if (mRecordingAudioRunnable != null) {
            mHandler.removeCallbacks(mRecordingAudioRunnable!!)
            mRecordingAudioRunnable = null
        }

        binding.floatingActionButtonSend.reset()

        binding.floatingActionButtonSend.morphToMic()

        binding.containerLockAudio.container.visibility = View.GONE
        recordingTime = 0L
        stopRecording()
    }

    private fun startRecording() {

        try {
            recordFile = FileManager.createFile(
                requireContext(),
                "${System.currentTimeMillis()}.aac",
                Constants.NapoleonCacheDirectories.AUDIOS.folder
            )

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                val fileOutputStream = FileOutputStream(recordFile!!)
                setOutputFile(fileOutputStream.fd)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                try {
                    prepare()

                    isRecordingAudio = true

                    mRecordingAudioRunnable = Runnable {
                        if (mRecordingAudioRunnable != null) {
                            val oneSecond = TimeUnit.SECONDS.toMillis(1)
                            recordingTime += oneSecond
                            binding.inputPanel.setRecordingTime(recordingTime)

                            mHandler.postDelayed(mRecordingAudioRunnable!!, oneSecond)
                        }
                    }

                    mHandler.postDelayed(mRecordingAudioRunnable!!, 1000)

                } catch (e: IOException) {
                    Timber.e("prepare() failed")
                }

                start()
            }
        } catch (e: Exception) {
            resetAudioRecording()
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            if (mRecordingAudioRunnable != null) {
                mHandler.removeCallbacks(mRecordingAudioRunnable!!)
                mRecordingAudioRunnable = null
            }
            binding.inputPanel.setRecordingTime(0L)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun deleteRecordFile() {
        recordFile?.let { file ->
            if (file.exists()) {
                file.delete()
            }
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

    //region Implementation FabSend.FabSendListener

    override fun checkRecordAudioPermission(successCallback: () -> Unit) {
        this@ConversationFragment.verifyPermission(
            Manifest.permission.RECORD_AUDIO,
            drawableIconId = R.drawable.ic_mic_primary,
            message = R.string.text_explanation_to_record_audio_attacment
        ) {
            successCallback()
        }
    }

    override fun onMicActionDown() {
        startRecording()

        binding.inputPanel.changeViewSwitcherToSlideToCancel()

        binding.containerLockAudio.container.slideUp(200)

        binding.containerLockAudio.container.post {

            binding.floatingActionButtonSend.setContainerLock(binding.containerLockAudio)
        }
    }

    override fun onMicActionUp(hasLock: Boolean, hasCancel: Boolean) {
        if (!hasLock && !hasCancel) {
            binding.inputPanel.changeViewSwitcherToInputPanel()
            if (mRecordingAudioRunnable != null) {
                mHandler.removeCallbacks(mRecordingAudioRunnable!!)
                mRecordingAudioRunnable = null
            }
            binding.containerLockAudio.container.visibility = View.GONE
            stopRecording()
        }
    }

    override fun onMicLocked() {
        isRecordingAudio = true
        val animTime = 200L

        binding.inputPanel.changeViewSwitcherToCancel()

        binding.containerLockAudio.container.animate().scaleX(1.5f).scaleY(1.5f)
            .setDuration(animTime).withEndAction {
                binding.containerLockAudio.container.animate().scaleX(1.0f).scaleY(1.0f)
                    .setDuration(animTime).withEndAction {
                        binding.containerLockAudio.container.animate().scaleX(1.5f).scaleY(1.5f)
                            .setDuration(animTime).withEndAction {
                                binding.containerLockAudio.container.animate().scaleX(1.0f)
                                    .scaleY(1.0f).setDuration(animTime).withEndAction {
                                        binding.containerLockAudio.container.apply {
                                            visibility = View.GONE

                                            val constraintSet = ConstraintSet()

                                            // clonamos el constrainSet del padre del elemento que vamos a modificar
                                            constraintSet.clone(binding.containerConversation)

                                            // Obtenemos el id del elemento a modificar
                                            val id = id

                                            constraintSet.constrainHeight(
                                                id,
                                                ConstraintSet.MATCH_CONSTRAINT
                                            )
                                            constraintSet.connect(
                                                id,
                                                ConstraintSet.BOTTOM,
                                                binding.floatingActionButtonSend.id,
                                                ConstraintSet.BOTTOM
                                            )

                                            constraintSet.applyTo(binding.containerConversation)
                                        }
                                    }
                            }
                    }
            }
    }

    override fun onMicCancel() {
        isRecordingAudio = false
        resetAudioRecording()
    }
    //endregion
}
