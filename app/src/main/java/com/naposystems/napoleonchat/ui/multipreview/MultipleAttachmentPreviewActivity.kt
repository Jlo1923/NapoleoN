package com.naposystems.napoleonchat.ui.multipreview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ActivityMultipleAttachmentPreviewBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.adapters.MultipleAttachmentFragmentAdapter
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewAction.*
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewMode
import com.naposystems.napoleonchat.ui.multipreview.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.multipreview.fragments.dialog.MultipleAttachmentRemoveAttachmentDialogFragment
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentRemoveListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.ViewAttachmentOptionsListener
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.MultipleAttachmentRemoveEvent
import com.naposystems.napoleonchat.ui.multipreview.listeners.events.ViewAttachmentOptionEvent
import com.naposystems.napoleonchat.ui.multipreview.model.MultipleAttachmentRemoveItem
import com.naposystems.napoleonchat.ui.multipreview.views.ViewMultipleAttachmentTabView
import com.naposystems.napoleonchat.ui.selfDestructTime.Location
import com.naposystems.napoleonchat.ui.selfDestructTime.SelfDestructTimeDialogFragment
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
import java.util.*
import javax.inject.Inject

class MultipleAttachmentPreviewActivity
    : AppCompatActivity(),
    ViewAttachmentOptionsListener,
    MultipleAttachmentPreviewListener,
    MultipleAttachmentRemoveListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: MultipleAttachmentPreviewViewModel

    private lateinit var viewBinding: ActivityMultipleAttachmentPreviewBinding

    private var adapter: MultipleAttachmentFragmentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MultipleAttachmentPreviewViewModel::class.java)

        lifecycle.addObserver(viewModel)

        viewBinding = ActivityMultipleAttachmentPreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
        extractContactFromExtras()
        extractFilesFromExtras()
        extractIsModeViewInConversation()
        defineListeners()
    }

    override fun changeVisibilityOptions() = viewModel.changeVisibilityOptions()

    override fun forceShowOptions() = viewModel.forceShowOptions()

    override fun onViewAttachmentOptionEvent(event: ViewAttachmentOptionEvent) {
        when (event) {
            ViewAttachmentOptionEvent.OnChangeSelfDestruction -> onChangeSelfDestruction()
            ViewAttachmentOptionEvent.OnDelete -> onDeleteItem()
        }
    }

    override fun onRemoveAttachment(event: MultipleAttachmentRemoveEvent) {
        when (event) {
            MultipleAttachmentRemoveEvent.OnRemoveForAll -> TODO()
            MultipleAttachmentRemoveEvent.OnRemoveForRecipient -> TODO()
            MultipleAttachmentRemoveEvent.OnRemoveForSender -> TODO()
            MultipleAttachmentRemoveEvent.OnSimpleRemove -> removeFileInCreating()
        }
    }

    private fun onChangeSelfDestruction() {
        val dialog = SelfDestructTimeDialogFragment.newInstance(
            0,
            Location.CONVERSATION
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
            viewPagerAttachments.offscreenPageLimit = 9
            viewPagerAttachments.adapter = adapter

            TabLayoutMediator(
                viewPreviewBottom.getTabLayout(),
                viewPagerAttachments
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
            MultipleAttachmentPreviewState.Loading -> showLoading()
            is MultipleAttachmentPreviewState.SuccessFilesAsPager -> showFilesAsPager(state.listFiles)
        }
    }

    private fun showFilesAsPager(listFiles: ArrayList<MultipleAttachmentFileItem>) {
        showPagerAndOptions()
        adapter = MultipleAttachmentFragmentAdapter(this, listFiles)
        configureTabsAndViewPager(listFiles)
        addListenerToPager()
        viewBinding.viewPreviewBottom.postDelayed(
            { extractSelectedIndex() }, 500
        )
    }

    private fun showLoading() = viewBinding.apply {
        progressLoader.show()
        hideViews(viewPagerAttachments, viewPreviewBottom, viewAttachmentOptions)
    }

    private fun showPagerAndOptions() = viewBinding.apply {
        progressLoader.hide()
        showViews(viewPagerAttachments, viewPreviewBottom, viewAttachmentOptions)
    }

    private fun handleActions(action: MultipleAttachmentPreviewAction) {
        when (action) {
            Exit -> exitPreview()
            ExitToConversation -> exitPreview()
            HideAttachmentOptions -> hideAnimAttachmentOptions()
            ShowAttachmentOptions -> showAnimAttachmentOptions()
            ShowAttachmentOptionsWithoutAnim -> showAttachmentOptionsWithoutAnim()
            HideFileTabs -> hideBottomTabs()
            is ShowSelectFolderName -> TODO()
            is SelectItemInTabLayout -> removeElementPager(action.indexItem)
            is ShowSelfDestruction -> showSelfDestruction(action.selfDestruction)
            is SendMessageToRemote -> sendMessageToRemote(action)
            RemoveAttachInCreate -> showBottomDialogForRemove(getTextForDialogForRemoveAttach())
            RemoveAttachForReceiver -> showBottomDialogForRemove(getTextForDialogForReceiver())
            RemoveAttachForSender -> showBottomDialogForRemove(getTextForDialogForSender())
        }
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
        val title = getString(R.string.multi_title_remove_file)
        val message = getString(R.string.multi_msg_remove_file)
        val option1 = getString(R.string.multi_button_accept)
        val cancel = getString(R.string.multi_button_cancel)
        return MultipleAttachmentRemoveItem(
            title = title,
            message = message,
            option1 = option1,
            cancelText = cancel
        )
    }

    private fun getTextForDialogForReceiver(): MultipleAttachmentRemoveItem {
        val title = getString(R.string.multi_title_remove_file)
        val message = getString(R.string.multi_msg_remove_file)
        val option1 = getString(R.string.multi_button_remove_for_me)
        val cancel = getString(R.string.multi_button_cancel)
        return MultipleAttachmentRemoveItem(
            title = title,
            message = message,
            option1 = option1,
            cancelText = cancel
        )
    }

    private fun getTextForDialogForSender(): MultipleAttachmentRemoveItem {
        val title = getString(R.string.multi_title_remove_file)
        val message = getString(R.string.multi_msg_remove_file)
        val option1 = getString(R.string.multi_button_remove_for_me)
        val option2 = getString(R.string.multi_button_remove_for_all)
        val cancel = getString(R.string.multi_button_cancel)
        return MultipleAttachmentRemoveItem(
            title = title,
            message = message,
            option1 = option1,
            option2 = option2,
            cancelText = cancel
        )
    }

    private fun sendMessageToRemote(action: SendMessageToRemote) =
        viewModel.sendMessageToRemote(action.messageEntity, action.attachments)

    private fun showSelfDestruction(selfDestruction: Int) {
        val iconSelfDestruction = getDrawableSelfDestruction(selfDestruction)
        viewBinding.viewAttachmentOptions.changeDrawableSelfDestructionOption(iconSelfDestruction)
    }

    private fun exitPreview() = finish()

    private fun hideBottomTabs() = viewBinding.viewPreviewBottom.hideTabLayout()

    private fun removeElementPager(indexItem: Int) =
        viewBinding.viewPreviewBottom.getTabLayout().apply {
            selectTab(getTabAt(indexItem))
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

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })

    private fun addListenerToViewPager() =
        viewBinding.viewPagerAttachments.registerOnPageChangeCallback(

            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.loadSelfDestructionTimeByIndex(position)
                    viewModel.validateMustMarkAsReaded(position)
                    viewBinding.apply { viewPreviewBottom.showTextByPosition(position) }
                }

            })


    private fun defineListeners() = viewBinding.apply {
        imageClose.setOnClickListener { finish() }
        viewAttachmentOptions.defineListener(this@MultipleAttachmentPreviewActivity)
        viewPreviewBottom.setOnClickListenerButton {
            viewModel.saveMessageAndAttachments(viewPreviewBottom.getTextInEdit())
        }
    }

    private fun handleSelectSelfDestruction(selfDestructTimeSelected: Int) {
        val selectedFileToSee = viewBinding.viewPagerAttachments.currentItem
        viewModel.updateSelfDestructionForItemPosition(selectedFileToSee, selfDestructTimeSelected)
        val iconSelfDestruction = getDrawableSelfDestruction(selfDestructTimeSelected)
        viewBinding.viewAttachmentOptions.changeDrawableSelfDestructionOption(iconSelfDestruction)
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