package com.naposystems.napoleonchat.ui.previewmulti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ActivityMultipleAttachmentPreviewBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.adapters.MultipleAttachmentFragmentAdapter
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.previewmulti.fragments.dialog.MultipleAttachmentRemoveAttachmentDialogFragment
import com.naposystems.napoleonchat.ui.previewmulti.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.ui.previewmulti.listeners.MultipleAttachmentRemoveListener
import com.naposystems.napoleonchat.ui.previewmulti.views.ViewMultipleAttachmentTabView
import com.naposystems.napoleonchat.ui.previewmulti.listeners.ViewAttachmentOptionsListener
import com.naposystems.napoleonchat.ui.previewmulti.listeners.events.MultipleAttachmentRemoveEvent
import com.naposystems.napoleonchat.ui.previewmulti.listeners.events.ViewAttachmentOptionEvent
import com.naposystems.napoleonchat.ui.previewmulti.model.MultipleAttachmentRemoveItem
import com.naposystems.napoleonchat.ui.selfDestructTime.Location
import com.naposystems.napoleonchat.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.napoleonchat.utility.anims.animHideSlideDown
import com.naposystems.napoleonchat.utility.anims.animHideSlideUp
import com.naposystems.napoleonchat.utility.anims.animShowSlideDown
import com.naposystems.napoleonchat.utility.anims.animShowSlideUp
import com.naposystems.napoleonchat.utility.business.getDrawableSelfDestruction
import com.naposystems.napoleonchat.utility.extensions.showViews
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_multiple_attachment_remove_attachment_dialog.view.*
import java.util.ArrayList
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
        extractFilesFromExtras()
        bindViewModel()
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
            MultipleAttachmentRemoveEvent.OnSimpleRemove -> removeFile()
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

    private fun removeFile() {
        val selectedIndexFileToDelete = viewBinding.viewPagerAttachments.currentItem
        viewModel.onDeleteElement(selectedIndexFileToDelete)
    }

    private fun onDeleteItem() {
        val textsForDialog = getTextForDialog()
        val dialogForDelete = MultipleAttachmentRemoveAttachmentDialogFragment(
            textsForDialog, this
        )
        dialogForDelete.show(
            supportFragmentManager,
            "MultipleAttachmentRemoveAttachmentDialogFragment"
        )
    }

    private fun getTextForDialog(): MultipleAttachmentRemoveItem {
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

    private fun extractFilesFromExtras() {
        intent.extras?.let { bundle ->
            val files = bundle.getParcelableArrayList<MultipleAttachmentFileItem>("test")
            files?.let {
                viewModel.defineListFiles(it)
            }
        }
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
    }

    private fun handleState(state: MultipleAttachmentPreviewState) {
        when (state) {
            MultipleAttachmentPreviewState.Error -> TODO()
            MultipleAttachmentPreviewState.Loading -> showLoading()
            is MultipleAttachmentPreviewState.SuccessFilesAsPager -> showFilesAsPager(state.listFiles)
        }
    }

    private fun showLoading() {

    }

    private fun showFilesAsPager(listFiles: ArrayList<MultipleAttachmentFileItem>) {
        adapter = MultipleAttachmentFragmentAdapter(this, listFiles)
        configureTabsAndViewPager(listFiles)
        addListenerToPager()
    }

    private fun handleActions(action: MultipleAttachmentPreviewAction) {
        when (action) {
            MultipleAttachmentPreviewAction.Exit -> exitPreview()
            MultipleAttachmentPreviewAction.HideAttachmentOptions -> hideAnimAttachmentOptions()
            MultipleAttachmentPreviewAction.ShowAttachmentOptions -> showAnimAttachmentOptions()
            MultipleAttachmentPreviewAction.ShowAttachmentOptionsWithoutAnim -> showAttachmentOptionsWithoutAnim()
            MultipleAttachmentPreviewAction.HideFileTabs -> hideBottomTabs()
            is MultipleAttachmentPreviewAction.ShowSelectFolderName -> TODO()
            is MultipleAttachmentPreviewAction.SelectItemInTabLayout -> removeElementPager(action.indexItem)
            is MultipleAttachmentPreviewAction.ShowSelfDestruction -> showSelfDestruction(action.selfDestruction)
        }
    }

    private fun showSelfDestruction(selfDestruction: Int) {
        val iconSelfDestruction = getDrawableSelfDestruction(selfDestruction)
        viewBinding.viewAttachmentOptions.changeDrawableSelfDestructionOption(iconSelfDestruction)
    }

    private fun exitPreview() {
        finish()
    }

    private fun hideBottomTabs() {
        viewBinding.viewPreviewBottom.hideTabLayout()
    }

    private fun removeElementPager(indexItem: Int) {
        viewBinding.viewPreviewBottom.getTabLayout().apply {
            selectTab(getTabAt(indexItem))
        }
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

            override fun onTabSelected(tab: TabLayout.Tab) =
                (tab.customView as ViewMultipleAttachmentTabView).selected(true)

            override fun onTabUnselected(tab: TabLayout.Tab) =
                (tab.customView as ViewMultipleAttachmentTabView).selected(false)

            override fun onTabReselected(tab: TabLayout.Tab) {
            }

        })

    private fun addListenerToViewPager() =
        viewBinding.viewPagerAttachments.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.loadSelfDestructionTimeByIndex(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })


    private fun defineListeners() = viewBinding.apply {
        imageClose.setOnClickListener { finish() }
        viewAttachmentOptions.defineListener(this@MultipleAttachmentPreviewActivity)
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