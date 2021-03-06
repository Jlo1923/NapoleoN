package com.naposystems.napoleonchat.ui.multipreview

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ActivityMultipleAttachmentPreviewBinding
import com.naposystems.napoleonchat.dialog.selfDestructTime.Location
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.conversation.ConversationViewModel
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.adapters.MultipleAttachmentFragmentAdapter
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction.*
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewMode
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewState.SuccessFilesAsPager
import com.naposystems.napoleonchat.ui.multipreview.fragments.dialog.MultipleAttachmentRemoveAttachmentDialogFragment
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentRemoveListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.ViewAttachmentOptionsListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.MultipleAttachmentRemoveEvent
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.ViewAttachmentOptionEvent
import com.naposystems.napoleonchat.ui.multipreview.model.MODE_CREATE
import com.naposystems.napoleonchat.ui.multipreview.model.MODE_RECEIVER
import com.naposystems.napoleonchat.ui.multipreview.model.MODE_SENDER
import com.naposystems.napoleonchat.ui.multipreview.model.MultipleAttachmentRemoveItem
import com.naposystems.napoleonchat.ui.multipreview.viewmodels.MultipleAttachmentPreviewItemViewModel
import com.naposystems.napoleonchat.ui.multipreview.viewmodels.MultipleAttachmentPreviewViewModel
import com.naposystems.napoleonchat.ui.multipreview.views.ViewMultipleAttachmentTabView
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.anims.animHideSlideDown
import com.naposystems.napoleonchat.utility.anims.animHideSlideUp
import com.naposystems.napoleonchat.utility.anims.animShowSlideDown
import com.naposystems.napoleonchat.utility.anims.animShowSlideUp
import com.naposystems.napoleonchat.utility.business.getDrawableSelfDestruction
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.extras.*
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_multiple_attachment_remove_attachment_dialog.view.*
import kotlinx.android.synthetic.main.napoleon_keyboard_sticker_fragment.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MultipleAttachmentPreviewActivity : AppCompatActivity(),
    ViewAttachmentOptionsListener,
    MultipleAttachmentPreviewListener,
    MultipleAttachmentRemoveListener {

    private var currentPosition: Int = 0

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val conversationViewModel: ConversationViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var viewModel: MultipleAttachmentPreviewViewModel
    private lateinit var viewModelItem: MultipleAttachmentPreviewItemViewModel
    private lateinit var viewBinding: ActivityMultipleAttachmentPreviewBinding
    private var adapter: MultipleAttachmentFragmentAdapter? = null

    private var mustRejectMarkAsReadTheFirstAttachment = false

    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MultipleAttachmentPreviewViewModel::class.java)

        viewModelItem = ViewModelProvider(this, viewModelFactory)
            .get(MultipleAttachmentPreviewItemViewModel::class.java)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        lifecycle.addObserver(viewModel)

        viewBinding = ActivityMultipleAttachmentPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
        viewModel.loading()
        validateMustRejectMarkAsReadTheFirstAttachment()
        extractContactFromExtras()
        extractFilesFromExtras()
        extractIsModeViewInConversation()
        defineListeners()
        viewModel.markWasInPreviewActivity()
    }

    override fun changeVisibilityOptions() = viewModel.changeVisibilityOptions()

    override fun forceShowOptions() = viewModel.forceShowOptions()

    override fun onViewAttachmentOptionEvent(event: ViewAttachmentOptionEvent) {
        when (event) {
            is ViewAttachmentOptionEvent.OnChangeSelfDestruction -> viewModel.onChangeSelfDestruction(
                event.iconSelfDestruction
            )
            ViewAttachmentOptionEvent.OnDelete -> onDeleteItem()
        }
    }

    override fun onRemoveAttachment(event: MultipleAttachmentRemoveEvent) {
        when (event) {
            MultipleAttachmentRemoveEvent.OnRemoveForAll -> removeAttachmentForAll(event)
            MultipleAttachmentRemoveEvent.OnRemoveForTheUser -> removeAttachmentForUser(event)
            MultipleAttachmentRemoveEvent.OnSimpleRemove -> removeFileInCreating()
        }
    }

    private fun removeAttachmentForUser(event: MultipleAttachmentRemoveEvent) {
        val selectedIndexFileToDelete = viewBinding.viewPagerAttachments.currentItem
        viewModel.onDeleteAttachmentForUser(selectedIndexFileToDelete)
    }

    private fun removeAttachmentForAll(event: MultipleAttachmentRemoveEvent) {
        val selectedIndexFileToDelete = viewBinding.viewPagerAttachments.currentItem
        viewModel.onDeleteAttachmentForAll(selectedIndexFileToDelete)
    }

    override fun markAttachmentAsRead(fileItem: MultipleAttachmentFileItem) {
        viewModel.markAttachmentVideoAsRead(fileItem)
    }

    override fun deleteAttachmentByDestructionTime(
        attachmentWebId: String,
        position: Int
    ) = viewModel.deleteAttachmentByDestructionTime(attachmentWebId, position)

    private fun onChangeSelfDestruction(action: MultipleAttachmentPreviewAction.OnChangeSelfDestruction) {
        val dialog = SelfDestructTimeDialogFragment.newInstance(
            action.contactId,
            Location.CONVERSATION,
            action.iconSelfDestruction
        )
        dialog.setListener(object :
            SelfDestructTimeDialogFragment.SelfDestructTimeListener {
            override fun onSelfDestructTimeChange(selfDestructTimeSelected: Int) {
                handleSelectSelfDestruction(selfDestructTimeSelected)
            }
        })
        dialog.show(supportFragmentManager, "SelfDestructTime")
    }

    private fun removeFileInCreating() {
        val selectedIndexFileToDelete = viewBinding.viewPagerAttachments.currentItem
        viewModel.onDeleteElementInCreating(selectedIndexFileToDelete)
    }

    private fun onDeleteItem() {
        /**
         * Necesitamos ir al viewmodel y validar si el usuario es el receptor o emisor, segun esta
         * informacion, debemos crear el mensaje de eliminacion del archivo y su respectiva accion
         * en cuanto al negocio
         */
        viewModel.onDeleteAttachment()
    }

    private fun extractFilesFromExtras() = intent.extras?.let { bundle ->
        val files = bundle.getParcelableArrayList<MultipleAttachmentFileItem>(MULTI_EXTRA_FILES)
        files?.let { viewModel.defineListFiles(it) }
    }

    private fun extractIsModeViewInConversation() = intent.extras?.let { bundle ->
        val modeOnlyView = bundle.getBoolean(MODE_ONLY_VIEW)
        val message = bundle.getString(MESSAGE_TEXT)
        viewModel.defineModeOnlyViewInConversation(modeOnlyView, message)
    }

    private fun validateMustRejectMarkAsReadTheFirstAttachment() = intent.extras?.let { bundle ->
        if (bundle.containsKey(MULTI_SELECTED)) {
            val index = bundle.getInt(MULTI_SELECTED)
            mustRejectMarkAsReadTheFirstAttachment = index != 0
        }
    }

    override fun blockPager() {
        viewBinding.viewPagerAttachments.isUserInputEnabled = false
    }

    override fun unBlockPager() {
        viewBinding.viewPagerAttachments.isUserInputEnabled = true
    }

    private fun extractSelectedIndex() = intent.extras?.let { bundle ->
        if (bundle.containsKey(MULTI_SELECTED)) {
            val index = bundle.getInt(MULTI_SELECTED)
            viewBinding.viewPreviewBottom.selectTab(index)
        }
    }

    private fun extractContactFromExtras() = intent.extras?.let { bundle ->
        val contact = bundle.getParcelable<ContactEntity>(MULTI_EXTRA_CONTACT)
        contact?.let { viewModel.setContact(it) }
    }

    private fun configureTabsAndViewPager(it: ArrayList<MultipleAttachmentFileItem>) {

        viewBinding.apply {

            // TODO: this can change and improvment
            viewPagerAttachments.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            viewPagerAttachments.adapter = adapter

            TabLayoutMediator(
                viewPreviewBottom.getTabLayout(),
                viewPagerAttachments,
                false, false
            ) { tab, position ->
                val view = ViewMultipleAttachmentTabView(viewBinding.root.context)
                view.bindFile(it[position])
                view.selected(position == 0)
                tab.customView = view
            }.attach()


        }
    }

    private fun bindViewModel() {
        viewModel.actions().observe(this, { handleActions(it) })
        viewModel.state.observe(this, { handleState(it) })
        viewModel.modes().observe(this, { handleMode(it) })
    }

    private fun handleMode(mode: MultipleAttachmentPreviewMode) {
        when (mode) {
            MultipleAttachmentPreviewMode.ModeCreate -> modeCreateAttachments()
            is MultipleAttachmentPreviewMode.ModeView -> modeViewAttachments(mode.messageText)
        }
    }

    private fun modeCreateAttachments() {
        viewBinding.apply {
            viewAttachmentOptions.configureElementsForCreate()
            viewPreviewBottom.configForCreate()
        }
    }

    private fun modeViewAttachments(message: String) {
        viewBinding.apply {
            viewAttachmentOptions.configureElementsForView()
            viewPreviewBottom.configForViewAttachments()
            viewPreviewBottom.setMessage(message)
        }
    }

    private fun handleState(state: MultipleAttachmentPreviewState) {
        when (state) {
            MultipleAttachmentPreviewState.Error -> TODO()
            MultipleAttachmentPreviewState.Loading -> showLoading(true)
            is SuccessFilesAsPager -> showFilesAsPager(state)
            MultipleAttachmentPreviewState.LoadingForSend -> showLoading(true)
        }
    }

    private fun showFilesAsPager(state: SuccessFilesAsPager) {
        showPagerAndOptions()
        adapter = MultipleAttachmentFragmentAdapter(this, state.listFiles)
        configureTabsAndViewPager(state.listFiles)
        addListenerToPager()
        state.indexToSelect?.let {
            /**
             * por aca entra cuando se ha hecho proceso de eliminacion dentro del preview
             */
            viewBinding.viewPreviewBottom.postDelayed(
                { selectElementInTabLayout(it) }, 200
            )
        } ?: run {
            /**
             * Por aca entra cuando desde el collage han seleccionado una posicion
             */
            viewBinding.viewPreviewBottom.postDelayed(
                { extractSelectedIndex() }, 200
            )
        }

    }

    private fun showLoading(showText: Boolean) = viewBinding.apply {
        progressLoader.show()
        hideViews(viewPagerAttachments, viewPreviewBottom, viewAttachmentOptions)
        textLoading.show(showText)
    }

    private fun showPagerAndOptions() = viewBinding.apply {
        progressLoader.hide()
        textLoading.hide()
        showViews(viewPagerAttachments, viewPreviewBottom, viewAttachmentOptions)
    }

    private fun handleActions(action: MultipleAttachmentPreviewAction) {
        when (action) {
            Exit -> exitToMultiFolders()
            ExitToConversation -> exitToConversation()
            HideAttachmentOptions -> hideAnimAttachmentOptions()
            ShowAttachmentOptions -> showAnimAttachmentOptions()
            ShowAttachmentOptionsWithoutAnim -> showAttachmentOptionsWithoutAnim()
            HideFileTabs -> hideBottomTabs()
            RemoveAttachInCreate -> showBottomDialogForRemove(getTextForDialogForRemoveAttach())
            RemoveAttachForReceiver -> showBottomDialogForRemove(getTextForDialogForReceiver())
            RemoveAttachForSender -> showBottomDialogForRemove(getTextForDialogForSender())
            is SelectItemInTabLayout -> selectElementInTabLayout(action.indexItem)
            is ShowSelfDestruction -> showSelfDestruction(action.selfDestruction)
            is ExitAndSendDeleteFiles -> exitPreviewAndSendDeleteFiles(action.listFilesForRemoveInCreate)
            is OnChangeSelfDestruction -> onChangeSelfDestruction(action)
            is ExitToConversationAndSendData -> exitToConversationAndSendData(action)
            is ShowUpload -> showOrHideUploadIcon(action.shouldShowUpload)
        }
    }

    private fun showOrHideUploadIcon(shouldShow: Boolean) {
        viewBinding.imageButtonState.isVisible = shouldShow
    }

    private fun exitToConversationAndSendData(action: ExitToConversationAndSendData) {
        val intentResult = Intent()
        val data = Bundle().apply {
            putParcelable(EXTRA_MULTI_MSG_TO_SEND, action.messageEntity)
            putParcelableArrayList(EXTRA_MULTI_ATTACHMENTS_TO_SEND, ArrayList(action.attachments))
        }
        intentResult.putExtras(data)
        setResult(RESULT_OK, intentResult)
        finish()
    }

    private fun exitPreviewAndSendDeleteFiles(
        listFilesForRemoveInCreate: List<MultipleAttachmentFileItem>
    ) {
        val intentResult = Intent()

        // TODO: validar cual dejar y cual quitar
        intentResult.putStringArrayListExtra(
            MULTI_EXTRA_FILES_DELETE,
            ArrayList(listFilesForRemoveInCreate.map { it.id.toString() })
        )
        intentResult.putExtras(Bundle().apply {
            this.putStringArrayList(
                MULTI_EXTRA_FILES_DELETE,
                ArrayList(listFilesForRemoveInCreate.map { it.id.toString() })
            )
        })

        setResult(RESULT_CANCELED, intentResult)
        finish()
    }

    private fun showBottomDialogForRemove(textsForDialog: MultipleAttachmentRemoveItem) {
        val dialogForDelete = MultipleAttachmentRemoveAttachmentDialogFragment(
            textsForDialog, this
        )
        dialogForDelete.show(
            supportFragmentManager,
            "MultipleAttachmentRemoveAttachmentDialogFragment"
        )
    }

    private fun getTextForDialogForRemoveAttach(): MultipleAttachmentRemoveItem {
        val title = getString(R.string.text_delete_attachment_title)
        val message = getString(R.string.text_delete_attachment)
        val option1 = getString(R.string.text_accept)
        val cancel = getString(R.string.text_cancel)
        return MultipleAttachmentRemoveItem(
            title = title,
            message = message,
            option1 = option1,
            cancelText = cancel,
            modeDelete = MODE_CREATE
        )
    }

    private fun getTextForDialogForReceiver(): MultipleAttachmentRemoveItem {
        val title = getString(R.string.text_delete_attachment_title)
        val message = getString(R.string.text_delete_attachment)
        val option1 = getString(R.string.multi_button_delete_for_me)
        val cancel = getString(R.string.text_cancel)
        return MultipleAttachmentRemoveItem(
            title = title,
            message = message,
            option1 = option1,
            cancelText = cancel,
            modeDelete = MODE_RECEIVER
        )
    }

    private fun getTextForDialogForSender(): MultipleAttachmentRemoveItem {
        val title = getString(R.string.text_delete_attachment_title)
        val message = getString(R.string.text_delete_attachment)
        val option1 = getString(R.string.multi_button_delete_for_me)
        val option2 = getString(R.string.multi_button_delete_for_all)
        val cancel = getString(R.string.text_cancel)
        return MultipleAttachmentRemoveItem(
            title = title,
            message = message,
            option1 = option1,
            option2 = option2,
            cancelText = cancel,
            modeDelete = MODE_SENDER
        )
    }

    private fun showSelfDestruction(selfDestruction: Int) {
        val iconSelfDestruction = getDrawableSelfDestruction(selfDestruction)
        viewBinding.viewAttachmentOptions.changeDrawableSelfDestructionOption(
            iconSelfDestruction,
            selfDestruction
        )
    }

    private fun exitToConversation() {
        val intentResult = Intent()
        setResult(RESULT_OK, intentResult)
        finish()
    }

    private fun exitToMultiFolders() {
        val intentResult = Intent()
        setResult(RESULT_CANCELED, intentResult)
        finish()
    }

    private fun hideBottomTabs() = viewBinding.viewPreviewBottom.hideTabLayout()

    private fun selectElementInTabLayout(indexItem: Int) =
        viewBinding.viewPreviewBottom.getTabLayout().apply {
            val indexSelect = if (tabCount == indexItem) {
                indexItem - 1
            } else {
                indexItem
            }
            selectTab(getTabAt(indexSelect))
        }

    private fun showAttachmentOptionsWithoutAnim() =
        viewBinding.apply {
            showViews(imageClose, viewAttachmentOptions, viewPreviewBottom)
        }

    private fun addListenerToPager() {
        addListenerToTabLayout()
        addListenerToViewPager()
    }

    private fun addListenerToTabLayout() = viewBinding.viewPreviewBottom.getTabLayout()
        .addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                (tab.customView as ViewMultipleAttachmentTabView).selected(true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) =
                (tab.customView as ViewMultipleAttachmentTabView).selected(false)

            override fun onTabReselected(tab: TabLayout.Tab) {}

        })

    private fun addListenerToViewPager() =
        viewBinding.viewPagerAttachments.registerOnPageChangeCallback(

            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition = position
                    viewModel.loadSelfDestructionTimeByIndex(position)
                    viewModel.validateShouldShowUpload(position)
                    if (position == 0) {
                        if (mustRejectMarkAsReadTheFirstAttachment.not()) {
                            viewModel.validateMustAttachmentMarkAsReaded(position)
                        } else {
                            mustRejectMarkAsReadTheFirstAttachment = false
                        }
                    } else {
                        viewModel.validateMustAttachmentMarkAsReaded(position)
                    }
                    viewBinding.apply { viewPreviewBottom.showTextByPosition(position) }
                }

            })


    private fun defineListeners() = viewBinding.apply {
        imageClose.setOnClickListener { viewModel.validateExitInCreateMode() }
        viewAttachmentOptions.defineListener(this@MultipleAttachmentPreviewActivity)
        viewPreviewBottom.setOnClickListenerButton {
            viewModel.saveMessageAndAttachments(viewPreviewBottom.getTextInEdit())
        }
        imageButtonState.setOnClickListener {
           /* if (Utils.isInternetAvailable(viewBinding.root.context)) {
               intent.extras?.getParcelableArrayList<AttachmentEntity?>(MULTI_EXTRA_ATTACHMENTS)?.let {  attachments ->
                   intent.extras?.getParcelable<MessageEntity>(MULTI_EXTRA_ENTITY) ?.let {entity ->
                       conversationViewModel.sendMessageToRemote(entity, listOf(attachments.get(currentPosition)))
                   }
                }

            } else {
                //showNotInternetMessage()
            }*/
        }
    }

    private fun handleSelectSelfDestruction(selfDestructTimeSelected: Int) {
        val selectedFileToSee = viewBinding.viewPagerAttachments.currentItem
        viewModel.updateSelfDestructionForItemPosition(selectedFileToSee, selfDestructTimeSelected)
        val iconSelfDestruction = getDrawableSelfDestruction(selfDestructTimeSelected)
        viewBinding.viewAttachmentOptions.changeDrawableSelfDestructionOption(
            iconSelfDestruction,
            selfDestructTimeSelected
        )
    }

    private fun hideAnimAttachmentOptions() = viewBinding.apply {
        imageClose.animHideSlideUp()
        viewAttachmentOptions.animHideSlideUp()
        viewPreviewBottom.animHideSlideDown()
    }

    private fun showAnimAttachmentOptions() = viewBinding.apply {
        imageClose.animShowSlideDown()
        viewAttachmentOptions.animShowSlideDown()
        viewPreviewBottom.animShowSlideUp()
    }

}