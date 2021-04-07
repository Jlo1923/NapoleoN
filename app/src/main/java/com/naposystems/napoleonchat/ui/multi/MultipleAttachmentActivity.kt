package com.naposystems.napoleonchat.ui.multi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ActivityMultipleAttachmentBinding
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.ui.contacts.showToast
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentAction
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.views.itemview.MultipleAttachmentFileItemView
import com.naposystems.napoleonchat.ui.multi.views.itemview.MultipleAttachmentFolderItemView
import com.naposystems.napoleonchat.ui.multipreview.MultipleAttachmentPreviewActivity
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.hideViews
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.extras.MULTI_EXTRA_CONTACT
import com.naposystems.napoleonchat.utility.extras.MULTI_EXTRA_FILES
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import dagger.android.AndroidInjection
import javax.inject.Inject

class MultipleAttachmentActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewBinding: ActivityMultipleAttachmentBinding

    private lateinit var viewModel: MultipleAttachmentViewModel

    private val groupieAdapter = GroupieAdapter()
    private val groupieAdapterFiles = GroupieAdapter()
    private lateinit var contact: ContactEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MultipleAttachmentViewModel::class.java)

        viewBinding = ActivityMultipleAttachmentBinding.inflate(layoutInflater)

        intent.extras?.let {
            if (it.containsKey(MULTI_EXTRA_CONTACT)) {
                contact = it.getSerializable(MULTI_EXTRA_CONTACT) as ContactEntity
            }
        }

        setContentView(viewBinding.root)
    }

    override fun onStart() {
        super.onStart()
        lifecycle.addObserver(viewModel)
        defineListeners()
        configRecyclerFolders()
        configRecyclerFiles()
        defineListenerItemsGroupie()
        bindViewModel()
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }

    override fun onBackPressed() = viewModel.handleBackAction()

    private fun bindViewModel() {
        viewModel.state.observe(this, { handleState(it) })
        viewModel.actions().observe(this, { handleActions(it) })
    }

    private fun handleActions(action: MultipleAttachmentAction) {
        when (action) {
            MultipleAttachmentAction.BackToFolderList -> showFoldersAgain()
            MultipleAttachmentAction.Exit -> finish()
            MultipleAttachmentAction.HideListSelectedFiles -> hidePreviewList()
            MultipleAttachmentAction.ShowHasMaxFilesAttached -> showMaxFilesAttached()
            is MultipleAttachmentAction.ContinueToPreview -> continueToPreview(action.listElements)
            is MultipleAttachmentAction.ShowSelectFolderName -> showFolderName(action.folderName)
            is MultipleAttachmentAction.ShowPreviewSelectedFiles -> showPreviewList(action.listElements)
        }
    }

    private fun continueToPreview(listElements: List<MultipleAttachmentFileItem>) {
        val intent = Intent(this, MultipleAttachmentPreviewActivity::class.java)
        intent.putExtras(Bundle().apply {
            putParcelable(MULTI_EXTRA_CONTACT, contact)
            putParcelableArrayList(MULTI_EXTRA_FILES, ArrayList(listElements))
        })
        startActivity(intent)
    }

    private fun showMaxFilesAttached() {
        viewBinding.root.context.apply {
            showToast(this, getString(R.string.multi_msg_cannot_share_more_ten))
        }
    }

    private fun hidePreviewList() = viewBinding.viewPreviewBottom.hide()

    private fun showPreviewList(listElements: List<Item<*>>) {
        viewBinding.viewPreviewBottom.apply {
            showElements(listElements)
            show(listElements.isEmpty().not())
        }
    }

    private fun handleState(state: MultipleAttachmentState) {
        when (state) {
            MultipleAttachmentState.Loading -> showLoading()
            is MultipleAttachmentState.SuccessFolders -> showFolders(state.listElements)
            is MultipleAttachmentState.SuccessFiles -> showFiles(state.listElements)
            MultipleAttachmentState.Error -> handleError()
        }
    }

    private fun defineListenerItemsGroupie() {
        defineListenerItemFolder()
        defineListenerItemFile()
    }

    private fun defineListenerItemFolder() = groupieAdapter.setOnItemClickListener { item, _ ->
        if (item is MultipleAttachmentFolderItemView) {
            viewModel.loadFilesFromFolder(item.item)
            groupieAdapterFiles.clear()
        }
    }

    private fun defineListenerItemFile() = groupieAdapterFiles.setOnItemClickListener { item, _ ->
        if (item is MultipleAttachmentFileItemView) {
            selectFileAsAttachment(item)
        }
    }

    private fun configRecyclerFolders() {
        groupieAdapter.spanCount = 2
        val layoutManager = GridLayoutManager(this, groupieAdapter.spanCount)
        viewBinding.recyclerFolders.apply {
            adapter = groupieAdapter
            setLayoutManager(layoutManager)
        }
    }

    private fun configRecyclerFiles() {
        groupieAdapterFiles.spanCount = 4
        val layoutManager = GridLayoutManager(this, groupieAdapterFiles.spanCount)
        viewBinding.recyclerFiles.apply {
            adapter = groupieAdapterFiles
            setLayoutManager(layoutManager)
        }
    }

    private fun selectFileAsAttachment(item: MultipleAttachmentFileItemView) =
        viewModel.tryAddToListAttachments(item)

    private fun defineListeners() = viewBinding.apply {
        imageBack.setOnClickListener { viewModel.handleBackAction() }
        viewPreviewBottom.setOnClickListenerButton {
            viewModel.continueToPreview()
        }
    }

    private fun showLoading() = viewBinding.apply {
        progress.show()
        hideViews(recyclerFolders, recyclerFiles)
    }

    private fun showFolders(listElements: List<Item<*>>) =
        groupieAdapter.updateAsync(listElements) { showFoldersList() }

    private fun showFiles(listElements: List<Item<*>>) =
        groupieAdapterFiles.updateAsync(listElements) { showFilesList() }

    private fun showFolderName(folderName: String) = viewBinding.textExplain.apply {
        text = folderName
        show(text.isEmpty().not())
    }

    private fun showFoldersAgain() {
        showFoldersList()
        showFolderName("")
    }

    private fun showFoldersList() = viewBinding.apply {
        recyclerFolders.show()
        hideViews(progress, recyclerFiles)
    }

    private fun showFilesList() = viewBinding.apply {
        recyclerFiles.show()
        hideViews(progress, recyclerFolders)
    }

    private fun handleError() = viewBinding.apply {
        hideViews(progress, recyclerFolders, recyclerFiles)
        Toast.makeText(root.context, "handleError", Toast.LENGTH_SHORT).show()
    }
}