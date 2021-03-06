package com.naposystems.napoleonchat.ui.conversation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.database.getStringOrNull
import androidx.core.graphics.toRect
import androidx.core.view.isVisible
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
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.databinding.ConversationActionBarBinding
import com.naposystems.napoleonchat.databinding.ConversationFragmentBinding
import com.naposystems.napoleonchat.dialog.deletionMesssages.DeletionMessagesDialogFragment
import com.naposystems.napoleonchat.dialog.muteConversation.MuteConversationDialogFragment
import com.naposystems.napoleonchat.dialog.selfDestructTime.Location
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogViewModel
import com.naposystems.napoleonchat.dialog.timeFormat.TimeFormatDialogViewModel
import com.naposystems.napoleonchat.dialog.userDisplayFormat.UserDisplayFormatDialogViewModel
import com.naposystems.napoleonchat.model.CallModel
import com.naposystems.napoleonchat.model.SubscriptionStatus
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.download.model.DownloadAttachmentResult
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.actionMode.ActionModeMenu
import com.naposystems.napoleonchat.ui.attachment.AttachmentDialogFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseViewModel
import com.naposystems.napoleonchat.ui.conversation.adapter.ConversationAdapter
import com.naposystems.napoleonchat.ui.conversation.adapter.helpers.ConversationListeners
import com.naposystems.napoleonchat.ui.conversation.adapter.helpers.ConversationViewModelsForViewHolders
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction.OpenMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction.SendMessageToRemote
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.listener.MultiAttachmentMsgListener
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.IncomingMultiAttachmentMsgViewModel
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels.MyMultiAttachmentMsgViewModel
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.conversationCall.ConversationCallActivity
import com.naposystems.napoleonchat.ui.custom.inputPanel.InputPanelWidget
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.multi.MultipleAttachmentActivity
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.MultipleAttachmentPreviewActivity
import com.naposystems.napoleonchat.ui.napoleonKeyboard.NapoleonKeyboard
import com.naposystems.napoleonchat.utility.*
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.Utils.Companion.showToast
import com.naposystems.napoleonchat.utility.adapters.verifyCameraAndMicPermission
import com.naposystems.napoleonchat.utility.adapters.verifyCameraAndMicPermissionForCall
import com.naposystems.napoleonchat.utility.adapters.verifyPermission
import com.naposystems.napoleonchat.utility.extensions.forMimeTypeNapoleon
import com.naposystems.napoleonchat.utility.extensions.toAttachmentEntityDocument
import com.naposystems.napoleonchat.utility.extras.*
import com.naposystems.napoleonchat.utility.helpers.ifNotNull
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerManager
import com.naposystems.napoleonchat.utility.sharedViewModels.contact.ContactSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationSharedViewModel
import com.naposystems.napoleonchat.utility.showCaseManager.ShowCaseManager
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.custom_input_panel_widget.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

class ConversationFragment
    : BaseFragment(),
    ConversationAdapter.ClickListener,
    InputPanelWidget.Listener,
    MultiAttachmentMsgListener,
    NapoleonKeyboard.InputTextMainListener {

    companion object {
        const val RC_DOCUMENT = 2511
        fun newInstance() = ConversationFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var handlerNotificationChannel: HandlerNotificationChannel

    @Inject
    lateinit var handlerDialog: HandlerDialog

    @Inject
    lateinit var mediaPlayerManager: MediaPlayerManager

    //TODO:Subscription
    /*@Inject
    lateinit var billingClientLifecycle: BillingClientLifecycle*/

    private val conversationViewModel: ConversationViewModel by viewModels {
        viewModelFactory
    }
    private val selfDestructTimeViewModel: SelfDestructTimeDialogViewModel by viewModels {
        viewModelFactory
    }

    private val sharedViewModel: ConversationSharedViewModel by activityViewModels()

    private val userDisplayFormatDialogViewModel: UserDisplayFormatDialogViewModel by activityViewModels {
        viewModelFactory
    }
    private val contactSharedViewModel: ContactSharedViewModel by viewModels {
        viewModelFactory
    }
    private val contactProfileSharedViewModel: ContactProfileSharedViewModel by activityViewModels {
        viewModelFactory
    }
    private val timeFormatShareViewModel: TimeFormatDialogViewModel by activityViewModels {
        viewModelFactory
    }

    private val baseViewModel: BaseViewModel by viewModels {
        viewModelFactory
    }

    private val myMultiAttachmentMsgViewModel: MyMultiAttachmentMsgViewModel by viewModels { viewModelFactory }
    private val incomingMultiAttachmentMsgViewModel: IncomingMultiAttachmentMsgViewModel by viewModels { viewModelFactory }

    private val documentsMimeTypeAllowed = arrayOf(
        Constants.MimeType.PDF.type,
        Constants.MimeType.DOC.type,
        Constants.MimeType.DOCX.type,
        Constants.MimeType.XLS.type,
        Constants.MimeType.XLSX.type,
        Constants.MimeType.PPT.type,
        Constants.MimeType.PPTX.type
    )

    private lateinit var actionBarCustomView: ConversationActionBarBinding
    private lateinit var binding: ConversationFragmentBinding
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val args: ConversationFragmentArgs by navArgs()
    private var isEditTextFilled: Boolean = false
    private lateinit var actionMode: ActionModeMenu
    private var menuOptionsContact: Menu? = null
    private lateinit var deletionMessagesDialog: DeletionMessagesDialogFragment
    private var showCase: ShowCaseManager? = null

    private var clipboard: ClipboardManager? = null
    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var clipData: ClipData? = null
    private var mRecordingAudioRunnable: Runnable? = null
    private var mQuotedMessage: Int? = null
    private var mNextAudioPosition: Int? = null

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

    //    private var messagedLoadedFirstTime: Boolean = false
    private var actionViewSchedule: View? = null
    private var isFabScroll = false
    private var enterConversation = false
    private var counterNotification = 0
    private var menuCreated: Boolean = false
    private var showShowCase: Boolean = false
    private var isSelectedMessage = false
    private var isValidMessagesPending = false

    private val mHandler: Handler by lazy {
        Handler()
    }

    private val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up)
    }
    private val animationScaleDown: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down)
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
            if (viewHolder.adapterPosition >= 0) {
                conversationAdapter.getMessageAndAttachment(viewHolder.adapterPosition)
                    ?.let { messageAndAttachment ->
                        if (messageAndAttachment.messageEntity.messageType == Constants.MessageTextType.NORMAL.type &&
                            (messageAndAttachment.messageEntity.status == Constants.MessageStatus.UNREAD.status ||
                                    messageAndAttachment.messageEntity.status == Constants.MessageStatus.READED.status ||
                                    messageAndAttachment.messageEntity.status == Constants.MessageStatus.SENT.status ||
                                    messageAndAttachment.getFirstAttachment()?.status == Constants.AttachmentStatus.DOWNLOAD_COMPLETE.status)
                        ) {
                            actionSwipeQuote(
                                actionState,
                                recyclerView,
                                dX,
                                viewHolder,
                                c,
                                messageAndAttachment
                            )

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
            }
        }
    }

    private val onScrollQuoteListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                SCROLL_STATE_IDLE -> {
                    conversationAdapter.startFocusAnimation(mQuotedMessage)
                    binding.recyclerViewConversation.removeOnScrollListener(this)
                    mQuotedMessage = null
                }
            }
        }
    }

    private val onScrollPLayNextAudioListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                SCROLL_STATE_IDLE -> {
                    mNextAudioPosition?.let {
                        conversationAdapter.notifyPlayAudio(it)
                        binding.recyclerViewConversation.removeOnScrollListener(this)
                        mNextAudioPosition = null
                    }
                }
            }
        }
    }

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        inflateCustomActionBar(inflater)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_fragment, container, false
        )

        emojiKeyboard = NapoleonKeyboard(
            binding.coordinator,
            binding.inputPanel.getEditText(),
            this
        )
        binding.inputPanel.getEditText().isCursorVisible = true
        binding.lifecycleOwner = this

        binding.contact = args.contact

        setupActionMode()

        setupAdapter()

        inputPanelFabClickListener()

        inputPanelEditTextWatcher()

        inputPanelAttachmentButtonClickListener()

        inputPanelCameraButtonClickListener()

        counterNotification = 0
        showCounterNotification()
        enterConversation = false

        binding.inputPanel.setListener(this)

        binding.buttonCall.setSafeOnClickListener {
//            if (checkBatteryOptimized()) {
            this.verifyCameraAndMicPermissionForCall {
                conversationViewModel.setIsVideoCall(false)
                conversationViewModel.callContact()
                binding.buttonCall.isEnabled = false
                binding.buttonVideoCall.isEnabled = false
            }
//            }
        }

        binding.buttonVideoCall.setSafeOnClickListener {
//            if (checkBatteryOptimized()) {
            this.verifyCameraAndMicPermissionForCall {
                conversationViewModel.setIsVideoCall(true)
                conversationViewModel.callContact()
                binding.buttonCall.isEnabled = false
                binding.buttonVideoCall.isEnabled = false
            }
//            }
        }

        binding.textViewReturnCall.setSafeOnClickListener {
            Timber.d("startCallActivity returnCall ConversationFragment")
            val intent = Intent(context, ConversationCallActivity::class.java).apply {
                putExtras(Bundle().apply {
                    putBoolean(ConversationCallActivity.ACTION_RETURN_CALL, true)
                })
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
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
                Handler().postDelayed({
                    val friendlyMessageCount: Int = conversationAdapter.itemCount - 1
                    val lastVisiblePosition: Int =
                        linearLayoutManager.findLastVisibleItemPosition()

                    val invisibleItems = friendlyMessageCount - lastVisiblePosition

                    isFabScroll =
                        if (invisibleItems >= Constants.QUANTITY_TO_SHOW_FAB_CONVERSATION) {
                            showFabScroll(View.VISIBLE, animationScaleUp)
                            true
                        } else {
                            showFabScroll(View.GONE, animationScaleDown)
                            showCounterNotification()
                            false
                        }
//                    Timber.d("*TestScroll: isFabScroll on addOnScrollListener $isFabScroll")
                    super.onScrolled(recyclerView, dx, dy)
                }, 200)
            }
        })

        binding.recyclerViewConversation.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.recyclerViewConversation.post {
//                    Timber.d("*TestScroll: isFabScroll on addOnLayoutChangeListener $isFabScroll")
                    if (!isFabScroll) {
                        val friendlyMessageCount: Int = conversationAdapter.itemCount
                        binding.recyclerViewConversation.scrollToPosition(friendlyMessageCount - 1)
                    }
                }
            }
        }

        clipboard = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        binding.textViewUserStatus.isSelected = true

        return binding.root
    }

    private fun inputPanelCameraButtonClickListener() {
        binding.inputPanel.getImageButtonCamera().setOnClickListener {
            verifyCameraAndMicPermission {
                findNavController().navigate(
                    ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                        conversationViewModel.getUser().id,
                        args.contact.id,
                        binding.inputPanel.getWebIdQuote(),
                        Constants.LocationImageSelectorBottomSheet.CONVERSATION.location,
                        binding.inputPanel.getEditText().text.toString().trim()
                    )
                )
            }
        }
    }

    private fun inputPanelAttachmentButtonClickListener() {
        binding.inputPanel.getImageButtonAttachment().setSafeOnClickListener {
            Utils.hideKeyboard(binding.inputPanel.getImageButtonAttachment())
            validateStateOutputControl()
            val attachmentDialog = AttachmentDialogFragment()
            attachmentDialog.setListener(object :
                AttachmentDialogFragment.OnAttachmentDialogListener {
                override fun galleryPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.text_explanation_to_send_audio_attachment
                    ) {

                        val intent = Intent(context, MultipleAttachmentActivity::class.java)
                        intent.putExtras(Bundle().apply {
                            putParcelable(MULTI_EXTRA_CONTACT, args.contact)
                        })
                        startActivityForResult(intent, MULTI_ATTACHMENT_INTENT)

//                        findNavController().navigate(
//                            ConversationFragmentDirections.actionConversationFragmentToAttachmentGalleryFoldersFragment(
//                                args.contact,
//                                binding.inputPanel.getWebIdQuote(),
//                                Constants.LocationImageSelectorBottomSheet.CONVERSATION.location,
//                                binding.inputPanel.getEditText().text.toString().trim()
//                            )
//                        )
                    }
                }

                override fun cameraPressed() {
                    this@ConversationFragment.verifyCameraAndMicPermission {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToConversationCameraFragment(
                                conversationViewModel.getUser().id,
                                args.contact.id,
                                binding.inputPanel.getWebIdQuote(),
                                Constants.LocationImageSelectorBottomSheet.CONVERSATION.location,
                                binding.inputPanel.getEditText().text.toString().trim()
                            )
                        )
                    }
                }

                override fun locationPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        drawableIconId = R.drawable.ic_location_on_primary,
                        message = R.string.text_explanation_to_send_location_attachment
                    ) {
                        findNavController().navigate(
                            ConversationFragmentDirections.actionConversationFragmentToAttachmentLocationFragment(
                                args.contact.id
                            )
                        )
                    }
                }

                override fun audioPressed() {
                    this@ConversationFragment.verifyPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        drawableIconId = R.drawable.ic_folder_primary,
                        message = R.string.text_explanation_to_send_audio_attachment
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
                        message = R.string.text_explanation_to_send_audio_attachment
                    ) {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "*/*"
                        startActivityForResult(intent, RC_DOCUMENT)
                    }
                }
            })
            attachmentDialog.show(parentFragmentManager, "attachmentDialog")
        }
    }

    private fun inputPanelEditTextWatcher() {
        binding.inputPanel.setEditTextWatcher(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.inputPanel.apply {
                    val text = getEditText().text.toString()
                    if (!binding.inputPanel.isRecordingInLockedMode()) {
                        val text = getEditText().text.toString()

                        if (text.isNotEmpty() && !isEditTextFilled) {
                            showImageButtonSend()
                            hideButtonRecord()
                            hideImageButtonCamera()
                            isEditTextFilled = true
                            containerWrap()
                        } else if (text.isEmpty()) {
                            hideImageButtonSend()
                            showButtonRecord()
                            showImageButtonCamera()
                            isEditTextFilled = false
                            containerNoWrap()
                        }
                    }
                }
            }
        })
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun inputPanelFabClickListener() {
        /*with(binding.floatingActionButtonSend) {
            this.setSafeOnClickListener {
                Timber.d("setOnClickListener")
                if (!this.isShowingMic() && !isRecordingAudio) {
                    viewModel.saveMessageLocally(
                        binding.inputPanel.getEditTex().text.toString().trim(),
                        obtainTimeSelfDestruct(),
                        binding.inputPanel.getQuote()?.message?.webId ?: ""
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
        }*/
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (emojiKeyboard?.isShowing() == true) {
                emojiKeyboard?.handleBackButton()
            } else {
                findNavController().popBackStack(R.id.search_all_fragment, false)
            }
        }

        subscribeRxEvents()

        sharedViewModel.hasAudioSendClicked.observe(requireActivity(), Observer {
            if (it == true) {
                sharedViewModel.getAudiosSelected().forEach { mediaStoreAudio ->
                    conversationViewModel.saveMessageWithAudioAttachment(
                        mediaStoreAudio,
                        obtainTimeSelfDestruct(),
                        binding.inputPanel.getWebIdQuote()
                    )
                }
            }
        })

        sharedViewModel.attachmentEntitySelected.observe(requireActivity(), Observer { attachment ->
            if (attachment != null) {
                val quote = binding.inputPanel.getQuote()
                sharedViewModel.setQuoteWebId(quote?.messageEntity?.webId ?: "")
                conversationViewModel.saveMessageAndAttachment(
                    ItemMessage(
                        messageString = sharedViewModel.getMessage() ?: "",
                        attachment = attachment,
                        numberAttachments = 1,
                        selfDestructTime = obtainTimeSelfDestruct(),
                        quote = sharedViewModel.getQuoteWebId() ?: ""
                    )
                )
            }
        })

        sharedViewModel.listAttachments.observe(requireActivity(), Observer { attachments ->
            attachments?.forEach {
                val quote = binding.inputPanel.getQuote()
                sharedViewModel.setQuoteWebId(quote?.messageEntity?.webId ?: "")
                conversationViewModel.saveMessageAndAttachment(
                    ItemMessage(
                        sharedViewModel.getMessage() ?: "",
                        it,
                        1,
                        obtainTimeSelfDestruct(),
                        sharedViewModel.getQuoteWebId() ?: ""
                    )
                )
            }
        })

        sharedViewModel.emojiSelected.observe(requireActivity(), Observer { emoji ->
            if (emoji != null) {
                binding.inputPanel.getEditText().text?.append(
                    EmojiCompat.get().process(String(emoji.code, 0, emoji.code.size))
                )
            }
        })

        sharedViewModel.gifSelected.observe(requireActivity(), Observer { gifAttachment ->
            try {
                if (gifAttachment != null) {
                    val quote = binding.inputPanel.getQuote()
                    sharedViewModel.setQuoteWebId(quote?.messageEntity?.webId ?: "")
                    this.findNavController().navigate(
                        ConversationFragmentDirections.actionConversationFragmentToAttachmentPreviewFragment(
                            gifAttachment,
                            0,
                            sharedViewModel.getQuoteWebId() ?: "",
                            message = binding.inputPanel.getEditText().text.toString(),
                            contactId = args.contact.id
                        )
                    )
                    sharedViewModel.resetGifSelected()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        })

    }

    override fun onStart() {
        super.onStart()
        validateMustGoToPreviewAttachmentsFromOutside()
        conversationViewModel.verifyMessagesReceived()
        conversationViewModel.verifyMessagesRead()
    }

    private fun getSubscriptionStatus() = SubscriptionStatus.valueOf(
        sharedPreferencesManager.getString(
            Constants.SharedPreferences.SubscriptionStatus,
            SubscriptionStatus.ACTIVE.name
        )
    )

    private fun shouldShowAttachmentPreview(subscriptionStatus: SubscriptionStatus) =
        subscriptionStatus == SubscriptionStatus.ACTIVE ||
                subscriptionStatus == SubscriptionStatus.FREE_TRIAL ||
                subscriptionStatus == SubscriptionStatus.FREE_TRIAL_DAY_4

    private fun validateMustGoToPreviewAttachmentsFromOutside() {
        val uris = conversationViewModel.getPendingUris()
        if (uris.isEmpty().not() && shouldShowAttachmentPreview(getSubscriptionStatus())) {
            if (uris.size <= 10) {
                handleIntentExtrasDataForMultiple(uris)
            } else {
                conversationViewModel.removePendingUris()
                showToast(binding.root.context, getString(R.string.multi_max_files_from_outside))
                activity?.finish()
            }
        }
    }

    private fun handleIntentExtrasDataForMultiple(uris: List<Uri>) {
        conversationViewModel.removePendingUris()
        val intent = Intent(requireContext(), MultipleAttachmentPreviewActivity::class.java)
        val listElements = uris.map {
            val mimeType = binding.root.context.contentResolver.getType(it)
            val mimeTypeForNapo = mimeType?.forMimeTypeNapoleon() ?: ""
            MultipleAttachmentFileItem(
                id = 0,
                attachmentType = mimeTypeForNapo,
                contentUri = it,
                isSelected = false,
                selfDestruction = 0
            )
        }
        intent.putExtras(Bundle().apply {
            putParcelable(MULTI_EXTRA_CONTACT, args.contact)
            putParcelableArrayList(MULTI_EXTRA_FILES, ArrayList(listElements))
        })
        startActivityForResult(intent, MULTI_ATTACHMENT_INTENT)
    }

    @InternalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //TODO:Subscription
//        lifecycle.addObserver(billingClientLifecycle)

        binding.viewModel = conversationViewModel

        NapoleonApplication.currentConversationContactId = args.contact.id

        cleanSelectionMessages()

        contactProfileSharedViewModel.getLocalContact(args.contact.id)

        conversationViewModel.setContact(args.contact)

        conversationViewModel.getLocalMessages()

        conversationViewModel.getMessagesSelected(args.contact.id)

        conversationViewModel.getMessageNotSent(args.contact.id)

        selfDestructTimeViewModel.getSelfDestructTimeByContact(args.contact.id)

        selfDestructTimeViewModel.getSelfDestructTime()

        baseViewModel.getOutputControl()

        timeFormatShareViewModel.getTimeFormat()

        selfDestructTimeViewModel.getDestructTimeByContact.observe(viewLifecycleOwner, Observer {
            selfDestructTimeViewModel.selfDestructTimeByContact = it
            setIconTimeDestruction()
        })

        observeMessagesSelected()

        observeWebServiceError()

        observeDeleteMessagesForAllWsError()

        observeMessageMessages()

        observeContactProfile()

        observeStringsCopy()

        observeResponseDeleteLocalMessages()

        conversationViewModel.contactCalledSuccessfully.observe(
            viewLifecycleOwner,
            Observer { channel ->
                if (!channel.isNullOrEmpty()) {
                    Timber.d("startCallActivity contactCalledSuccessfully")

                    NapoleonApplication.callModel = CallModel(
                        contactId = args.contact.id,
                        channelName = channel,
                        isVideoCall = conversationViewModel.isVideoCall(),
                        typeCall = Constants.TypeCall.IS_OUTGOING_CALL,
                        mustSubscribeToPresenceChannel = true
                    )

                    val intent = Intent(context, ConversationCallActivity::class.java)
                    startActivity(intent)
                    (context as MainActivity).overridePendingTransition(
                        R.anim.slide_in_up,
                        R.anim.slide_out_down
                    )
                    conversationViewModel.resetContactCalledSuccessfully()
                    conversationViewModel.resetIsVideoCall()
                    binding.buttonCall.isEnabled = true
                    binding.buttonVideoCall.isEnabled = true
                } else {
                    binding.buttonCall.isEnabled = true
                    binding.buttonVideoCall.isEnabled = true
                }
            })

        conversationViewModel.downloadAttachmentProgress.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewConversation.post {
                when (it) {
                    is DownloadAttachmentResult.Start -> {
                        conversationAdapter.setStartDownload(it.itemPosition, it.job)
                    }
                    is DownloadAttachmentResult.Progress -> {
                        conversationAdapter.setDownloadProgress(
                            it.itemPosition,
                            it.progress
                        )
                    }
                    is DownloadAttachmentResult.Error -> {
                        it.attachmentEntity.status =
                            Constants.AttachmentStatus.DOWNLOAD_ERROR.status
                        conversationViewModel.updateAttachment(
                            it.attachmentEntity
                        )
                        Timber.d("Error")
                        conversationAdapter.setDownloadCancel(it.itemPosition)
                    }
                    is DownloadAttachmentResult.Cancel -> {
                        conversationAdapter.setDownloadCancel(it.itemPosition)
                    }
                    is DownloadAttachmentResult.Success -> {
                        conversationAdapter.setDownloadComplete(it.itemPosition)
                    }
                }
            }
        })

        conversationViewModel.stateMessage.observe(viewLifecycleOwner, {
            if (it != null) {
                Timber.d("--- State ${it}")
                when (it) {
                    is StateMessage.Start -> {
                        conversationAdapter.setStateMessage(
                            it.messageId,
                            Constants.StateMessage.START.state
                        )
                    }
                    is StateMessage.Success -> {
                        conversationAdapter.setStateMessage(
                            it.messageId,
                            Constants.StateMessage.SUCCESS.state
                        )
                    }
                    is StateMessage.Error -> {
                        conversationAdapter.setStateMessage(
                            it.messageId,
                            Constants.StateMessage.ERROR.state
                        )
                    }
                }
            }
        })

        conversationViewModel.documentCopied.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val quote = binding.inputPanel.getQuote()
                sharedViewModel.setQuoteWebId(quote?.messageEntity?.webId ?: "")
                val attachment = it.toAttachmentEntityDocument()

                conversationViewModel.saveMessageAndAttachment(
                    ItemMessage(
                        attachment = attachment,
                        numberAttachments = 1,
                        selfDestructTime = obtainTimeSelfDestruct(),
                        quote = sharedViewModel.getQuoteWebId() ?: ""
                    )
                )
                conversationViewModel.resetDocumentCopied()
                binding.inputPanel.closeQuote()
            }
        })

        conversationViewModel.noInternetConnection.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                handlerDialog.alertDialogInformative(
                    getString(R.string.text_alert_failure),
                    getString(if (conversationViewModel.isVideoCall()) R.string.text_video_call_not_internet_connection else R.string.text_call_not_internet_connection),
                    true,
                    requireContext(),
                    R.string.text_close
                ) {
                    binding.buttonCall.isEnabled = true
                    binding.buttonVideoCall.isEnabled = true
                }
            }
            binding.buttonCall.isEnabled = true
            binding.buttonVideoCall.isEnabled = true
        })

        conversationViewModel.newMessageSend.observe(viewLifecycleOwner, Observer { newMessage ->
            if (newMessage == true) {
                binding.inputPanel.post {
                    binding.inputPanel.clearTextEditText()
                }
                conversationViewModel.resetNewMessage()
            }
        })

        conversationViewModel.messageNotSentEntity.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.inputPanel.getEditText().setText(it.message)
            }
        }

        contactSharedViewModel.conversationDeleted.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        })

        //TODO:Subscription
        /*billingClientLifecycle.purchases.observe(viewLifecycleOwner, Observer { purchasesList ->
            purchasesList?.let {
                for (purchase in purchasesList) {
                    billingClientLifecycle.acknowledged(purchase)
                }
                Timber.d("Billing purchases $purchasesList")
                billingClientLifecycle.queryPurchasesHistory()
            }
        })

        billingClientLifecycle.purchasesHistory.observe(
            viewLifecycleOwner,
            Observer { purchasesHistory ->
                purchasesHistory?.let {
                    val freeTrial = viewModel.getFreeTrial()

                    if (System.currentTimeMillis() > freeTrial) {
                        if (purchasesHistory.isEmpty()) {
                            binding.inputPanel.isVisible = false
                            binding.buttonCall.isVisible = false
                            binding.buttonVideoCall.isVisible = false
                        } else {
                            try {
                                val dateExpireSubscriptionMillis =
                                    getDataSubscription(purchasesHistory)
                                if (System.currentTimeMillis() > dateExpireSubscriptionMillis) {
                                    binding.inputPanel.isVisible = false
                                    binding.buttonCall.isVisible = false
                                    binding.buttonVideoCall.isVisible = false
                                }
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }
                    } else {
                        binding.inputPanel.isVisible = true
                        binding.buttonCall.isVisible = true
                        binding.buttonVideoCall.isVisible = true
                    }
                }
            })*/
    }

    /*private fun getDataSubscription(purchasesHistory: List<PurchaseHistoryRecord>): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = purchasesHistory[0].purchaseTime
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val lastPurchase = purchasesHistory[0]

        when (lastPurchase.sku) {
            Constants.SkuSubscriptions.MONTHLY.sku -> calendar.add(
                Calendar.MONTH,
                Constants.SubscriptionsTimeType.MONTHLY.subscription
            )

            Constants.SkuSubscriptions.SEMIANNUAL.sku -> calendar.add(
                Calendar.MONTH,
                Constants.SubscriptionsTimeType.SEMIANNUAL.subscription
            )

            else -> calendar.add(Calendar.YEAR, Constants.SubscriptionsTimeType.YEARLY.subscription)
        }

        val dateExpireSubscription = sdf.parse(sdf.format(calendar.time))
        return dateExpireSubscription!!.time
    }*/

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun saveAndSendRecordAudio() {
        recordFile?.let { file ->
            val quote = binding.inputPanel.getQuote()
            sharedViewModel.setQuoteWebId(quote?.messageEntity?.webId ?: "")

            val attachment = AttachmentEntity(
                id = 0,
                messageId = 0,
                webId = "",
                messageWebId = "",
                uuid = UUID.randomUUID().toString(),
                type = Constants.AttachmentType.AUDIO.type,
                body = "",
                fileName = file.name,
                origin = Constants.AttachmentOrigin.RECORD_AUDIO.origin,
                thumbnailUri = "",
                status = Constants.AttachmentStatus.SENDING.status,
                extension = "aac",
                duration = recordingTime
            )

            recordingTime = 0L
            isRecordingAudio = false
            stopRecording()

            conversationViewModel.saveMessageAndAttachment(
                ItemMessage(
                    attachment = attachment,
                    numberAttachments = 1,
                    selfDestructTime = obtainTimeSelfDestruct(),
                    quote = sharedViewModel.getQuoteWebId() ?: ""
                )
            )
        }
    }

    private fun observeResponseDeleteLocalMessages() {
        conversationViewModel.responseDeleteLocalMessages.observe(viewLifecycleOwner, Observer {
            if (it && actionMode.mode != null) {
                actionMode.mode!!.finish()
            }
        })
    }

    private fun observeStringsCopy() {
        conversationViewModel.stringsCopy.observe(viewLifecycleOwner, Observer {
            if (it.count() == 1) {
                copyDataInClipboard(conversationViewModel.parsingListByTextBlock(it))
                conversationViewModel.resetListStringCopy()
                Toast.makeText(context, R.string.text_message_copied, Toast.LENGTH_LONG).show()
                actionMode.mode!!.finish()
            }
        })
    }

    private fun observeContactProfile() {
        contactProfileSharedViewModel.contact.observe(viewLifecycleOwner, Observer { contact ->
            if (contact != null) {
                actionBarCustomView.contact = contact
                setTextSilenceOfMenu(contact)
            }
        })
    }

    private fun observeMessageMessages() {
        conversationViewModel.messageMessagesRelation.observe(
            viewLifecycleOwner,
            Observer { conversationList ->
                if (conversationList.isNotEmpty()) {
                    if(isValidMessagesPending == false){
                        isValidMessagesPending = true
                        //Poner en estado fallido mensajes que no se enviaron cuando se cerro la app

                        conversationList.forEach {
                            if (it.messageEntity.status == Constants.MessageStatus.SENDING.status
                                && it.messageEntity.webId.isNullOrEmpty() == true
                                && it.messageEntity.body.isNullOrEmpty() == false){

                                var messageEntity = it.messageEntity
                                messageEntity.status = Constants.MessageStatus.ERROR.status
                                conversationViewModel.updateMessage(messageEntity)

                            }
                        }

                    }

                    conversationAdapter.submitList(conversationList) {
                        if (enterConversation) {
                            validScroll(conversationList, conversationAdapter.itemCount)
                        } else {
                            binding.recyclerViewConversation.scrollToPosition(conversationAdapter.itemCount - 1)
                            enterConversation = true
                        }
                    }
                    //conversationAdapter.notifyDataSetChanged()
//                Timber.d("*TestMessage: ${conversationList.last()}")
                    conversationViewModel.sendTextMessagesRead()
                } else {
                    conversationAdapter.submitList(conversationList)
                    isValidMessagesPending = true
                }

            }
        )
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun subscribeRxEvents() {
        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (args.contact.id == it.contactId) {
                        if (args.contact.stateNotification) {
                            handlerNotificationChannel.deleteUserChannel(
                                args.contact.id,
                                args.contact.getNickName()
                            )
                        }
                        findNavController().popBackStack()
                    }
                }

        val disposableIncomingCall = RxBus.listen(RxEvent.IncomingCall::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onRecorderReleased()
                binding.inputPanel.cancelRecording()
            }

        val disposableIncomingCallSystem = RxBus.listen(RxEvent.IncomingCallSystem::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                onRecorderReleased()
                binding.inputPanel.cancelRecording()
            }

        val disposableNewMessageEvent =
            RxBus.listen(RxEvent.NewMessageEventForCounter::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (args.contact.id == it.contactId && isFabScroll) {
                        counterNotification++
                        showCounterNotification()
                    }
                }

        val disposableContactHasHangup = RxBus.listen(RxEvent.CallEnd::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

                Timber.d("LLAMADA PASO: DISPOSE CALL OYENDO DESDE LA CONVERSACION")
                binding.textViewReturnCall.isVisible = false
                binding.buttonCall.isEnabled = true
                binding.buttonVideoCall.isEnabled = true
            }

        val disposableUploadStart = RxBus.listen(RxEvent.UploadStart::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("RxEvent.UploadStart")
                conversationAdapter.setUploadStart(
                    it.attachmentEntity
                )
            }

        val disposableUploadSuccess = RxBus.listen(RxEvent.UploadSuccess::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("RxEvent.UploadSuccess")
                conversationAdapter.setUploadComplete(it.attachmentEntity)
            }

        val disposableUploadError = RxBus.listen(RxEvent.UploadError::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("RxEvent.UploadError")
            }

        val disposableUploadProgress = RxBus.listen(RxEvent.UploadProgress::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("RxEvent.UploadProgress: ${it.progress}")
                conversationAdapter.setUploadProgress(
                    it.attachmentEntity,
                    it.progress
                )
            }

        val disposableCompressProgress = RxBus.listen(RxEvent.CompressProgress::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("RxEvent.CompressProgress")
                conversationAdapter.setCompressProgress(
                    it.attachmentEntity,
                    it.progress
                )
            }

        val disposableStateFlag = RxBus.listen(RxEvent.StateFlag::class.java)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Timber.d("RxEvent.StateFlag")
                if (it.state == Constants.StateFlag.ON.state)
                    requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

        disposable.add(disposableContactBlockOrDelete)
        disposable.add(disposableIncomingCall)
        disposable.add(disposableNewMessageEvent)
        disposable.add(disposableContactHasHangup)
        disposable.add(disposableUploadStart)
        disposable.add(disposableUploadSuccess)
        disposable.add(disposableUploadError)
        disposable.add(disposableUploadProgress)
        disposable.add(disposableCompressProgress)
        disposable.add(disposableIncomingCallSystem)
        disposable.add(disposableStateFlag)
    }

    private fun validScroll(
        conversationList: List<MessageAttachmentRelation>,
        friendlyMessageCount: Int
    ) {
        if (conversationList.last().messageEntity.isMine == Constants.IsMine.YES.value &&
            conversationList.last().messageEntity.status == Constants.MessageStatus.SENDING.status
        ) {
            counterNotification = 0
            showCounterNotification()
            binding.recyclerViewConversation.scrollToPosition(friendlyMessageCount - 1)
        }
        if (!isFabScroll && actionMode.mode == null && !isSelectedMessage) {
            binding.recyclerViewConversation.scrollToPosition(friendlyMessageCount - 1)
        } else isSelectedMessage = false

    }

    private fun showCounterNotification() {
        if (isFabScroll && counterNotification > 0) {
            when {
                (counterNotification in 0..99) -> {
                    binding.textViewNotificationMessage.visibility = View.VISIBLE
                    binding.textViewNotificationMessage.text = counterNotification.toString()
                }
                (counterNotification > 99) -> {
                    binding.textViewNotificationMessage.text = "+99"
                }
            }
        } else {
            binding.textViewNotificationMessage.visibility = View.GONE
            counterNotification = 0
        }
    }

    private fun observeDeleteMessagesForAllWsError() {
        conversationViewModel.deleteMessagesForAllWsError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showSnackbar(it)
            }
        })
    }

    private fun observeWebServiceError() {
        conversationViewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                showSnackbar(it)
            }
        })
    }

    private fun observeMessagesSelected() {
        conversationViewModel.messagesSelected.observe(
            viewLifecycleOwner, Observer { listMessageAndAttachment ->

                val quantityMessagesOtherUser = listMessageAndAttachment.filter {
                    it.messageEntity.isMine == 0
                }.toList().count()

                val quantityMessagesFailed = listMessageAndAttachment.filter {
                    it.messageEntity.status == Constants.MessageStatus.ERROR.status
                }.toList().count()

                val quantitySystemMessage = listMessageAndAttachment.filter {
                    it.messageEntity.messageType == Constants.MessageTextType.NEW_CONTACT.type
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
                            if (messageAndAttachment.attachmentEntityList.count() > Constants.QUANTITY_ATTACHMENTS) {
                                actionMode.hideCopyButton = true
                            }
                        }
                        actionMode.quantityMessageOtherUser = quantityMessagesOtherUser
                        actionMode.quantityMessagesFailed = quantityMessagesFailed
                        actionMode.quantitySystemMessage = quantitySystemMessage
                    }

                    else -> {
                        actionMode.hideCopyButton = true
                        actionMode.quantityMessageOtherUser = quantityMessagesOtherUser
                        actionMode.quantityMessagesFailed = quantityMessagesFailed
                        actionMode.quantitySystemMessage = quantitySystemMessage
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
        snackbarUtils.showSnackbar {}
    }

    private fun handlerGoDown() {
        counterNotification = 0
        showCounterNotification()
        val friendlyMessageCount: Int = conversationAdapter.itemCount
        binding.recyclerViewConversation.smoothScrollToPosition(friendlyMessageCount - 1)
    }

    /*override fun onDetach() {
        with(activity as MainActivity) {
            supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
            supportActionBar?.setDisplayShowCustomEnabled(false)

        }
        super.onDetach()
    }*/

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_conversation, menu)
        menuOptionsContact = menu

        val scheduleMenuItem = menu.findItem(R.id.menu_item_schedule)
        actionViewSchedule = scheduleMenuItem.actionView
        actionViewSchedule?.setOnClickListener {
            onOptionsItemSelected(scheduleMenuItem)
        }

        contactProfileSharedViewModel.contact.value?.let { contact ->
            setTextSilenceOfMenu(contact)
        }

        menuCreated = true

        showCase()

        //subscription
        when (getSubscriptionStatus()) {
            SubscriptionStatus.PARTIAL_LOCK -> {
                menu.findItem(R.id.menu_item_delete_messages).isVisible = false
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setIconTimeDestruction() {
        menuOptionsContact?.let {
            actionViewSchedule?.let { actionView ->

                ((actionView as FrameLayout).getChildAt(0) as ImageView).setImageDrawable(
                    when (obtainTimeSelfDestruct()) {
                        Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time ->
                            requireContext().getDrawable(R.drawable.ic_five_seconds)

                        Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time ->
                            requireContext().getDrawable(R.drawable.ic_fifteen_seconds)

                        Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time ->
                            requireContext().getDrawable(R.drawable.ic_thirty_seconds)

                        Constants.SelfDestructTime.EVERY_ONE_MINUTE.time ->
                            requireContext().getDrawable(R.drawable.ic_one_minute)

                        Constants.SelfDestructTime.EVERY_TEN_MINUTES.time ->
                            requireContext().getDrawable(R.drawable.ic_ten_minutes)

                        Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time ->
                            requireContext().getDrawable(R.drawable.ic_thirty_minutes)

                        Constants.SelfDestructTime.EVERY_ONE_HOUR.time ->
                            requireContext().getDrawable(R.drawable.ic_one_hour)

                        Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time ->
                            requireContext().getDrawable(R.drawable.ic_twelve_hours)

                        Constants.SelfDestructTime.EVERY_ONE_DAY.time ->
                            requireContext().getDrawable(R.drawable.ic_one_day)

                        Constants.SelfDestructTime.EVERY_SEVEN_DAY.time ->
                            requireContext().getDrawable(R.drawable.ic_seven_days)

                        else -> null
                    }
                )
            }
        }
    }

    private fun setTextSilenceOfMenu(contact: ContactEntity) {
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
        NapoleonApplication.currentConversationContactId = args.contact.id
        showCase?.setPaused(false)
        showCase()
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        Timber.d("onResume")
        setConversationBackground()

        with(NapoleonApplication.statusCall.isConnectedCall()) {
            binding.textViewReturnCall.isVisible = this
            binding.buttonCall.isEnabled = !this
            binding.buttonVideoCall.isEnabled = !this
        }
        //messagedLoadedFirstTime = false
        if (binding.inputPanel.getEditText().text.toString().isNotEmpty()) {
            binding.inputPanel.apply {
                showImageButtonSend()
                hideButtonRecord()
                hideImageButtonCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        NapoleonApplication.currentConversationContactId = Constants.UserNotExist.USER_NO_EXIST.user
        resetConversationBackground()
        mediaPlayerManager.unregisterProximityListener()
        mediaPlayerManager.resetMediaPlayer()
        emojiKeyboard?.dispose()
        disposable.dispose()
        if (mRecordingAudioRunnable != null) {
            mHandler.removeCallbacks(mRecordingAudioRunnable!!)
            mRecordingAudioRunnable = null
        }
        FileManager.deleteTempsFiles(requireContext())
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
                blockContact()
            }
            R.id.menu_item_mute_conversation -> {
                contactProfileSharedViewModel.contact.value?.let { contact ->
                    if (contact.silenced)
                        disableSilence()
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

        if (requestCode == MULTI_ATTACHMENT_INTENT) {
            data?.let { handleMultiAttachmentResult(it) }
        } else {
            if (requestCode != RC_DOCUMENT || resultCode != RESULT_OK)
                return

            Timber.d("URI FILE: ${data?.data.toString()}")

            data?.data?.let { uri ->
                try {
                    handleActivityResultOld(uri)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }


    }

    private fun handleMultiAttachmentResult(data: Intent) {

        data.extras?.apply {
            val msg = this.getParcelable<MessageEntity>(EXTRA_MULTI_MSG_TO_SEND)
            val attachments =
                this.getParcelableArrayList<AttachmentEntity>(EXTRA_MULTI_ATTACHMENTS_TO_SEND)

            ifNotNull(msg, attachments) { msg, attachments ->
                conversationViewModel.sendMessageToRemote(msg, attachments)
            }

        }

    }

    private fun handleActivityResultOld(uri: Uri) {
        val contentResolver = requireContext().contentResolver

        val cursor = contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE
            ),
            null,
            null,
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            cursor.use {
                val mimeTypeIndex =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val mimeType = cursor.getStringOrNull(mimeTypeIndex)

                val sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val size = cursor.getInt(sizeIndex)

                if (!documentsMimeTypeAllowed.contains(mimeType)) {
                    handlerDialog.generalDialog(
                        getString(R.string.text_title_attach_doc),
                        getString(R.string.text_attch_doc_not_allowed),
                        false,
                        childFragmentManager,
                        getString(R.string.text_okay)
                    ) {

                    }
                } else if (size > Constants.MAX_DOCUMENT_FILE_SIZE) {
                    handlerDialog.generalDialog(
                        getString(R.string.text_title_attach_doc),
                        getString(R.string.text_attch_doc_size_exceed),
                        false,
                        childFragmentManager,
                        getString(R.string.text_okay)
                    ) {

                    }
                } else {
                    Timber.d("DocumentAttachment $mimeType, $size")
                    conversationViewModel.sendDocumentAttachment(uri)
                }
            }
        }
    }

    private fun blockContact() {
        handlerDialog.generalDialog(
            getString(R.string.text_block_contact),
            getString(R.string.text_wish_block_contact),
            true,
            childFragmentManager
        ) {
            contactSharedViewModel.sendBlockedContact(args.contact)
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun silenceConversation() {
        contactProfileSharedViewModel.contact.value?.let { contact ->
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

    private fun disableSilence() {
        contactProfileSharedViewModel.contact.value?.let { contact ->
            contactSharedViewModel.muteConversation(args.contact.id, contact.silenced)
        }
    }

    private fun deleteConversation() {
        handlerDialog.generalDialog(
            getString(R.string.text_title_delete_conversation),
            getString(R.string.text_want_delete_conversation),
            true,
            childFragmentManager
        ) {
            contactSharedViewModel.deleteConversation(args.contact.id)
        }
    }

    private fun copyDataInClipboard(text: String) {
        clipData = ClipData.newPlainText("text", text)
        clipboard?.setPrimaryClip(clipData!!)
    }

    private fun setConversationBackground() {
        val chatBackgroundFileName = conversationViewModel.getUser().chatBackground
        context?.let { context ->
            if (chatBackgroundFileName.isNotEmpty()) {
                val uri = Utils.getFileUri(
                    context = context,
                    fileName = chatBackgroundFileName,
                    subFolder = Constants.CacheDirectories.CHAT_BACKGROUND.folder
                )
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                val backgroundDrawable = Drawable.createFromStream(inputStream, uri.toString())
                binding.imageViewBackground.setImageDrawable(backgroundDrawable)
            } else {
                binding.imageViewBackground.setBackgroundColor(
                    Utils.convertAttrToColorResource(
                        requireContext(),
                        R.attr.attrBackgroundConversation
                    )
                )
            }
        }
    }

    private fun obtainTimeSelfDestruct(): Int {
        return selfDestructTimeViewModel.selfDestructTimeByContact?.let {
            if (it < 0) selfDestructTimeViewModel.selfDestructTimeGlobal.value
            else it
        } ?: kotlin.run { -1 }
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

        val params = ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )

        val toolbar = (activity as MainActivity).supportActionBar
        toolbar?.let { actionBar ->
            with(actionBar) {
                displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(false)
                setHomeButtonEnabled(false)
                setDisplayShowCustomEnabled(true)
                setHasOptionsMenu(true)
                setCustomView(actionBarCustomView.root, params)
            }
        }

        actionBarCustomView.contact = args.contact

        actionBarCustomView.userDisplayFormat =
            userDisplayFormatDialogViewModel.getUserDisplayFormat()

        actionBarCustomView.containerBack.setOnClickListener {
            findNavController().popBackStack()
        }

        actionBarCustomView.containerProfile.setOnClickListener {
            Handler().postDelayed({
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToContactProfileFragment(args.contact.id)
                )
            }, 100)
        }
    }

    private fun setupActionMode() {
        actionMode = ActionModeMenu(
            clickCopy = {
                conversationViewModel.copyMessagesSelected(args.contact.id)
            },
            clickDelete = { moreMessagesOtherContact ->
                //subscription
                if(getSubscriptionStatus() != SubscriptionStatus.PARTIAL_LOCK){
                    if (moreMessagesOtherContact) {
                        dialogWithoutNeutralButton(Constants.DeleteMessages.BY_SELECTION.option)
                    } else {
                        dialogWithNeutralButton(Constants.DeleteMessages.BY_SELECTION.option)
                    }
                }else{
                    Toast.makeText(requireContext(), R.string.text_free_trial_expired, Toast.LENGTH_SHORT).show()
                }

            }, clickBack = {
                cleanSelectionMessages()
                isSelectedMessage = true
//                Toast.makeText(context, "is Back", Toast.LENGTH_SHORT).show()
            })
    }

    private fun dialogWithNeutralButton(status: Int) {
        conversationViewModel.messagesSelected.value?.let { messagesSelected ->
            handlerDialog.alertDialogWithNeutralButton(
                R.string.text_delete_messages,
                false, requireContext(),
                R.string.text_delete_message_for_me,
                R.string.text_cancel,
                R.string.text_delete_message_for_all,
                clickTopButton = { _ ->
                    when (status) {
                        Constants.DeleteMessages.BY_SELECTION.option -> {
                            conversationViewModel.deleteMessagesSelected(
                                args.contact.id,
                                messagesSelected
                            )
                        }
                        Constants.DeleteMessages.BY_UNREADS.option -> {
                            conversationViewModel.deleteMessagesByStatusForMe(
                                args.contact.id,
                                status
                            )
                        }
                        Constants.DeleteMessages.BY_UNRECEIVED.option -> {
                            conversationViewModel.deleteMessagesByStatusForMe(
                                args.contact.id,
                                status
                            )
                        }
                    }
                },
                clickDownButton = { _ ->
                    when (status) {
                        Constants.DeleteMessages.BY_SELECTION.option -> {
                            conversationViewModel.deleteMessagesForAll(
                                args.contact.id,
                                messagesSelected
                            )
                        }
                        Constants.DeleteMessages.BY_UNREADS.option -> {
                            conversationViewModel.deleteMessagesByStatusForAll(
                                args.contact.id,
                                status
                            )
                        }
                        Constants.DeleteMessages.BY_UNRECEIVED.option -> {
                            conversationViewModel.deleteMessagesByStatusForAll(
                                args.contact.id,
                                status
                            )
                        }
                    }
                }
            )
        }
    }

    private fun dialogWithoutNeutralButton(status: Int) {
        conversationViewModel.messagesSelected.value?.let { listMessagesAndAttachments ->
            handlerDialog.alertDialogWithoutNeutralButton(
                R.string.text_delete_messages,
                false,
                requireContext(),
                Constants.LocationAlertDialog.CONVERSATION.location,
                R.string.text_accept,
                R.string.text_cancel,
                clickPositiveButton = { _ ->
                    when (status) {
                        Constants.DeleteMessages.BY_SELECTION.option -> {
                            conversationViewModel.deleteMessagesSelected(
                                args.contact.id,
                                listMessagesAndAttachments
                            )
                        }
                        Constants.DeleteMessages.BY_FAILED.option -> {
                            conversationViewModel.deleteMessagesByStatusForMe(
                                args.contact.id,
                                status
                            )
                        }
                    }
                }, clickNegativeButton = {}
            )
        }
    }

    @InternalCoroutinesApi
    private fun setupAdapter() {

        val viewModels = ConversationViewModelsForViewHolders(
            myMultiAttachmentMsgViewModel,
            incomingMultiAttachmentMsgViewModel
        )

        val listeners = ConversationListeners(
            listenerMultiAttachment = this@ConversationFragment
        )

        conversationAdapter = ConversationAdapter(
            this,
            mediaPlayerManager,
            timeFormatShareViewModel.getValTimeFormat(),
            listeners,
            viewModels
        )

        linearLayoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewConversation.setHasFixedSize(false)
        binding.recyclerViewConversation.adapter = conversationAdapter
        binding.recyclerViewConversation.layoutManager = linearLayoutManager
        binding.recyclerViewConversation.itemAnimator = null

        /*conversationAdapter.registerAdapterDataObserver(object :
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
        })*/
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewConversation)
    }

    private fun conversationAdapterOnClickEvent(item: MessageAttachmentRelation) {
        if (actionMode.mode != null) {
            updateStateSelectionMessage(item.messageEntity)
        }
    }

    private fun showFabScroll(visible: Int, animation: Animation) {
        if (visible != binding.fabGoDown.visibility) {
            binding.fabGoDown.visibility = visible
            binding.fabGoDown.startAnimation(animation)
        }
    }

    private fun updateStateSelectionMessage(item: MessageEntity) {
        conversationViewModel.updateStateSelectionMessage(args.contact.id, item.id, item.isSelected)
    }

    private fun cleanSelectionMessages() {
        conversationViewModel.cleanSelectionMessages(args.contact.id)
    }

    private fun openAttachmentDocument(attachmentEntity: AttachmentEntity) {

        try {
            var tempFile: File? = null

            val uri: Uri = if (BuildConfig.ENCRYPT_API) {
                tempFile = FileManager.createTempFileFromEncryptedFile(
                    requireContext(),
                    attachmentEntity.type,
                    "${attachmentEntity.webId}.${attachmentEntity.extension}",
                    attachmentEntity.extension
                )!!
                FileProvider.getUriForFile(
                    requireContext(),
                    "com.naposystems.napoleonchat.provider",
                    tempFile
                )
            } else {
                Utils.getFileUri(
                    requireContext(),
                    attachmentEntity.fileName,
                    Constants.CacheDirectories.DOCUMENTS.folder
                )
            }

            val intent = Intent(Intent.ACTION_VIEW)

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
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun actionSwipeQuote(
        actionState: Int,
        recyclerView: RecyclerView,
        dX: Float,
        viewHolder: RecyclerView.ViewHolder,
        c: Canvas,
        messageAndAttachmentRelation: MessageAttachmentRelation
    ) {
        val icon = resources.getDrawable(R.drawable.ic_quote_new, null)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            recyclerView.setOnTouchListener { _, event ->
                swipeBack = event?.action == MotionEvent.ACTION_UP
                if (swipeBack && dX > recyclerView.width / maxPositionSwipe) {
                    binding.inputPanel.resetImage()

                    if (messageAndAttachmentRelation.messageEntity.status != Constants.MessageStatus.ERROR.status) {
                        binding.inputPanel.openQuote(messageAndAttachmentRelation)
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

    @InternalCoroutinesApi
    override fun onPause() {
        conversationViewModel.sendMessageRead(mediaPlayerManager.getMessageId(), "")
        if (binding.inputPanel.getEditText().text.toString().trim() != "") {
            conversationViewModel.insertMessageNotSent(
                binding.inputPanel.getEditText().text.toString(),
                args.contact.id
            )
        } else {
            conversationViewModel.deleteMessageNotSent(args.contact.id)
        }
        super.onPause()
        mediaPlayerManager.unregisterProximityListener()
        mediaPlayerManager.resetMediaPlayer()
        if (binding.inputPanel.getEditText().text.toString().count() <= 0) {
            binding.inputPanel.cancelRecording()
        }
        NapoleonApplication.currentConversationContactId = Constants.UserNotExist.USER_NO_EXIST.user
        stopRecording()
        showCase?.setPaused(true)
        showCase?.dismiss()
        showShowCase = false
        resetAudioRecording()
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
//        binding.inputPanel.changeViewSwitcherToInputPanel()
        if (mRecordingAudioRunnable != null) {
            mHandler.removeCallbacks(mRecordingAudioRunnable!!)
            mRecordingAudioRunnable = null
        }

//        binding.floatingActionButtonSend.reset()

//        binding.floatingActionButtonSend.morphToMic()

//        binding.containerLockAudio.container.visibility = View.GONE
        recordingTime = 0L
        stopRecording()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    private fun startRecording() {
        try {
            recordFile = FileManager.createFile(
                requireContext(),
                "${System.currentTimeMillis()}.mp3",
                Constants.CacheDirectories.AUDIOS.folder
            )

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                AcousticEchoCanceler.isAvailable()
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                val fileOutputStream = FileOutputStream(recordFile!!)
                setOutputFile(fileOutputStream.fd)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                try {
                    prepare()

                    mRecordingAudioRunnable = Runnable {
                        if (mRecordingAudioRunnable != null) {
                            val oneSecond = TimeUnit.SECONDS.toMillis(1)
                            recordingTime += oneSecond
                            binding.inputPanel.setRecordingTime(recordingTime)

                            if (recordingTime == Constants.MAX_AUDIO_RECORD_TIME) {
                                isRecordingAudio = false
                                saveAndSendRecordAudio()
                                binding.inputPanel.releaseRecordingLock()
                            } else {
                                isRecordingAudio = true
                                mHandler.postDelayed(mRecordingAudioRunnable!!, oneSecond)
                            }
                        }
                        RxBus.publish(RxEvent.EnableButtonPlayAudio(false))
                    }

                    mHandler.postDelayed(mRecordingAudioRunnable!!, 1000)

                } catch (e: IOException) {
                    RxBus.publish(RxEvent.EnableButtonPlayAudio(true))
                    Timber.e("prepare() failed")
                }

                start()
            }
        } catch (e: Exception) {
            RxBus.publish(RxEvent.EnableButtonPlayAudio(true))
            stopRecording()
        }
    }

    private fun stopRecording() {
        RxBus.publish(RxEvent.EnableButtonPlayAudio(true))
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
            recordingTime = 0L
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            //Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 150)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    //region Implementation InputPanelWidget.Listener

    override fun checkRecordAudioPermission(successCallback: () -> Unit) {
        this@ConversationFragment.verifyPermission(
            Manifest.permission.RECORD_AUDIO,
            drawableIconId = R.drawable.ic_mic_primary,
            message = R.string.text_explanation_to_record_audio_attacment
        ) {
            successCallback()
        }
    }

    @InternalCoroutinesApi
    override fun onRecorderStarted() {
        mediaPlayerManager.resetMediaPlayer()
        startRecording()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 100)
    }

    @InternalCoroutinesApi
    override fun onRecorderReleased() {
        if (mRecordingAudioRunnable != null) {
            mHandler.removeCallbacks(mRecordingAudioRunnable!!)
            mRecordingAudioRunnable = null
        }
        if (isRecordingAudio && recordingTime >= minTimeRecording) {
            saveAndSendRecordAudio()
        }
        binding.inputPanel.closeQuote()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onRecorderLocked() {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onRecorderCanceled() {
        isRecordingAudio = false
        stopRecording()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Utils.vibratePhone(context, Constants.Vibrate.DEFAULT.type, 200)
    }

    @InternalCoroutinesApi
    override fun onSendButtonClicked() {
        when {
            binding.inputPanel.isRecordingInLockedMode() && recordingTime >= minTimeRecording -> {
                binding.inputPanel.releaseRecordingLock()
            }
            !isRecordingAudio -> {

                val quote = binding.inputPanel.getQuote()

                conversationViewModel.saveMessageLocally(
                    binding.inputPanel.getEditText().text.toString().trim(),
                    obtainTimeSelfDestruct(),
                    quote?.messageEntity?.webId ?: ""
                )

                with(binding.inputPanel.getEditText()) {
                    setText("")
                }
                binding.inputPanel.closeQuote()
            }
            else -> {
                binding.inputPanel.cancelRecording()
            }
        }

    }

    //endregion

    //region Implementation ConversationAdapter.ClickListener
    override fun onClick(item: MessageAttachmentRelation) {
        conversationAdapterOnClickEvent(item)
    }

    override fun onLongClick(messageEntity: MessageEntity) {
        if (actionMode.mode == null) {
            actionMode.startActionMode(view, R.menu.menu_selection_message)
            updateStateSelectionMessage(messageEntity)
        }
    }

    override fun messageToEliminate(item: MessageAttachmentRelation) {
        val messages = arrayListOf<MessageAttachmentRelation>()
        messages.add(item)
        conversationViewModel.deleteMessagesSelected(args.contact.id, messages)
    }

    override fun errorPlayingAudio() {
        Utils.showSimpleSnackbar(
            binding.coordinator,
            getString(R.string.text_error_playing_audio),
            3
        )
    }

    override fun onPreviewClick(item: MessageAttachmentRelation) {
        if (item.attachmentEntityList.isNotEmpty()) {
            val firstAttachment = item.attachmentEntityList.first()

            if (firstAttachment.type == Constants.AttachmentType.DOCUMENT.type) {
                if (item.messageEntity.status == Constants.MessageStatus.UNREAD.status && item.messageEntity.isMine == Constants.IsMine.NO.value) {
                    conversationViewModel.sendMessageRead(item)
                }
                openAttachmentDocument(firstAttachment)
            } else {
                findNavController().navigate(
                    ConversationFragmentDirections
                        .actionConversationFragmentToPreviewMediaFragment(item)
                )
            }
        }
    }

    override fun goToQuote(
        messageAndAttachmentRelation: MessageAttachmentRelation,
        itemPosition: Int?
    ) {
        val position = conversationViewModel.getMessagePosition(messageAndAttachmentRelation)

        if (position != -1) {
            binding.recyclerViewConversation.apply {
                removeOnScrollListener(onScrollQuoteListener)
                mQuotedMessage = position
                addOnScrollListener(onScrollQuoteListener)

                val firstItemVisible = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                Timber.d("firstItemVisible: $firstItemVisible, position: $position")

                if (firstItemVisible > position) {
                    binding.recyclerViewConversation.smoothScrollToPosition(position)
                } else {
                    conversationAdapter.startFocusAnimation(mQuotedMessage)
                }
            }
        }
    }

    override fun downloadAttachment(
        messageAndAttachmentRelation: MessageAttachmentRelation,
        itemPosition: Int?
    ) {
        Timber.d("downloadAttachment")
        if (itemPosition != null && messageAndAttachmentRelation.getFirstAttachment() != null) {
            conversationViewModel.downloadAttachment(messageAndAttachmentRelation, itemPosition)
        }
    }

    override fun uploadAttachment(
        attachmentEntity: AttachmentEntity,
        messageEntity: MessageEntity
    ) {
        conversationViewModel.uploadAttachment(
            attachmentEntity,
            messageEntity,
            obtainTimeSelfDestruct()
        )
    }

    override fun updateAttachmentState(attachmentEntity: AttachmentEntity) {
        conversationViewModel.updateAttachment(attachmentEntity)
    }

    override fun sendMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation) {
        Timber.d("sendMessageRead")
        conversationViewModel.sendMessageRead(messageAndAttachmentRelation)
    }

    override fun sendMessageRead(
        messageId: Int,
        webId: String,
        isComplete: Boolean,
        position: Int
    ) {
        conversationViewModel.sendMessageRead(messageId, webId)

        if (isComplete) {
            conversationAdapter.checkIfNextIsAudio(messageId)
        }
    }

    override fun reSendMessage(messageEntity: MessageEntity) {
        conversationViewModel.reSendMessage(messageEntity, obtainTimeSelfDestruct())
    }

    override fun scrollToNextAudio(nextPosition: Int) {
        binding.recyclerViewConversation.smoothScrollToPosition(nextPosition)

        binding.recyclerViewConversation.apply {
            removeOnScrollListener(onScrollPLayNextAudioListener)
            mNextAudioPosition = nextPosition
            addOnScrollListener(onScrollPLayNextAudioListener)

            val lastItemVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition()

            Timber.d("lastItemVisible: $lastItemVisible, position: $nextPosition")

            if (lastItemVisible < nextPosition) {
                Timber.d("scrollTo $nextPosition")
                binding.recyclerViewConversation.smoothScrollToPosition(nextPosition)
            } else {
                conversationAdapter.notifyPlayAudio(nextPosition)
            }
        }
    }

    override fun updateMessageState(messageEntity: MessageEntity) {
        conversationViewModel.updateMessage(messageEntity)
    }

    private fun showCase() {
        if (menuCreated && !showShowCase) {
            showCase = ShowCaseManager().apply {
                setActivity(requireActivity())
                setSeventhView(actionViewSchedule!!)
                showFromSeventh()
            }
            showShowCase = true
        }
    }

    override fun onMultipleAttachmentMsgAction(action: MultiAttachmentMsgAction) {
        when (action) {
            is OpenMultipleAttachmentPreview -> openMultipleAttachmentPreview(action)
            is SendMessageToRemote -> sendMessageAndAttachmentsToRemote(action)
            MultiAttachmentMsgAction.ShowNotInternetMessage -> showNotInternetMessage()
        }
    }

    private fun showNotInternetMessage() {
        Utils.showToast(binding.root.context, getString(R.string.msg_not_connection))
    }

    private fun sendMessageAndAttachmentsToRemote(action: SendMessageToRemote) {
        if (Utils.isInternetAvailable(binding.root.context)) {
            conversationViewModel.sendMessageToRemote(action.messageEntity, action.attachments)
        } else {
            showNotInternetMessage()
        }
    }

    /**
     * Este metodo es el punto de entrada para ver los attachments de un item de conversacion
     */
    private fun openMultipleAttachmentPreview(action: OpenMultipleAttachmentPreview) {
        val intent = Intent(requireContext(), MultipleAttachmentPreviewActivity::class.java)
        intent.putExtras(Bundle().apply {
            putParcelable(MULTI_EXTRA_CONTACT, args.contact)
            putParcelable(MULTI_EXTRA_ENTITY, action.messageEntity)
            action.attachments?.let {
                putParcelableArrayList(
                    MULTI_EXTRA_ATTACHMENTS,
                    ArrayList(it)
                )
            }
            putParcelableArrayList(MULTI_EXTRA_FILES, ArrayList(action.listElements))
            putInt(MULTI_SELECTED, action.index)
            action.message?.let { putString(MESSAGE_TEXT, it) }
            putBoolean(MODE_ONLY_VIEW, true)
        })
        startActivity(intent)
    }

    //NapoleonInputTextMainListener
    override fun isShowInputTextMain(value: Boolean) {
        binding.inputPanel.viewSwitcher.isVisible = value
    }

    override fun updateIconEmoji(showEmoji: Boolean) {
        val image = if (showEmoji) {
            R.drawable.ic_insert_emoticon_black
        } else {
            R.drawable.ic_insert_emoticon_black
        }
        binding.inputPanel.getImageButtonEmoji()
            .setImageDrawable(ContextCompat.getDrawable(requireContext(), image))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emojiKeyboard?.updateKeyboardStateClosed()
    }


    private fun setupSubscription(subscriptionStatus: SubscriptionStatus) {
        when (subscriptionStatus) {
            SubscriptionStatus.PARTIAL_LOCK -> {
                binding.inputPanel.isVisible = false
                binding.buttonCall.isVisible = false
                binding.buttonVideoCall.isVisible = false
                binding.containerStatus.isVisible = false
                binding.textViewUserStatus.isVisible = false

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val subscriptionStatus =
            sharedPreferencesManager.getString(
                Constants.SharedPreferences.SubscriptionStatus,
                SubscriptionStatus.ACTIVE.name
            )
        setupSubscription(SubscriptionStatus.valueOf(subscriptionStatus))
    }
    //endregion
}


