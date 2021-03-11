package com.naposystems.napoleonchat.ui.multi

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.naposystems.napoleonchat.databinding.ActivityMultipleAttachmentBinding
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentAction
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.views.MultipleAttachmentFileItemView
import com.naposystems.napoleonchat.ui.multi.views.MultipleAttachmentFolderItemView
import com.naposystems.napoleonchat.utility.extensions.hide
import com.naposystems.napoleonchat.utility.extensions.show
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.Item
import dagger.android.AndroidInjection
import javax.inject.Inject

class MultipleAttachmentActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivityMultipleAttachmentBinding

    private lateinit var viewModel: MultipleAttachmentViewModel

    private val groupieAdapter = GroupieAdapter()
    private val groupieAdapterFiles = GroupieAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MultipleAttachmentViewModel::class.java)

        super.onCreate(savedInstanceState)
        binding = ActivityMultipleAttachmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

    private fun bindViewModel() {
        viewModel.state.observe(this, { handleState(it) })
        viewModel.actions().observe(this, { handleActions(it) })
    }

    private fun handleActions(action: MultipleAttachmentAction) {
        when (action) {
            MultipleAttachmentAction.BackToFolderList -> showFoldersAgain()
            MultipleAttachmentAction.Exit -> finish()
            MultipleAttachmentAction.HideListSelectedFiles -> Unit
            is MultipleAttachmentAction.ShowListSelectedFiles -> showFolderName(action.folderName)
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

    override fun onBackPressed() {
        viewModel.handleBackAction()
    }

    private fun defineListenerItemsGroupie() {
        groupieAdapter.setOnItemClickListener { item, _ ->
            if (item is MultipleAttachmentFolderItemView) {
                viewModel.loadFilesFromFolder(item.item.folderName)
                groupieAdapterFiles.clear()
            }
        }
        groupieAdapterFiles.setOnItemClickListener { item, _ ->
            if (item is MultipleAttachmentFileItemView) {
                selectFileAsAttachment(item)
            }
        }
    }

    private fun configRecyclerFolders() {
        groupieAdapter.spanCount = 2
        val layoutManager = GridLayoutManager(this, groupieAdapter.spanCount)
        binding.recyclerFolders.apply {
            adapter = groupieAdapter
            setLayoutManager(layoutManager)
        }
    }

    private fun configRecyclerFiles() {
        groupieAdapterFiles.spanCount = 4
        val layoutManager = GridLayoutManager(this, groupieAdapterFiles.spanCount)
        binding.recyclerFiles.apply {
            adapter = groupieAdapterFiles
            setLayoutManager(layoutManager)
        }
    }

    private fun selectFileAsAttachment(item: MultipleAttachmentFileItemView) {
        item.isSelected = item.isSelected.not()
        if (item.isSelected) {
            viewModel.addFileToList(item.item)
        } else {
            viewModel.removeFileToList(item.item)
        }
    }

    private fun defineListeners() {
        binding.apply {
            imageBack.setOnClickListener { viewModel.handleBackAction() }
        }
    }

    private fun showLoading() = binding.apply {
        progress.show()
        recyclerFolders.hide()
        recyclerFiles.hide()
    }

    private fun showFolders(listElements: List<Item<*>>) =
        groupieAdapter.updateAsync(listElements) { showFoldersList() }

    private fun showFiles(listElements: List<Item<*>>) =
        groupieAdapterFiles.updateAsync(listElements) { showFilesList() }


    private fun showFolderName(folderName: String) = binding.textExplain.apply {
        text = folderName
        show(text.isEmpty().not())
    }

    private fun showFoldersAgain() {
        showFoldersList()
        showFolderName("")
    }

    private fun showFoldersList() = binding.apply {
        progress.hide()
        recyclerFolders.show()
        recyclerFiles.hide()
    }

    private fun showFilesList() = binding.apply {
        progress.hide()
        recyclerFiles.show()
        recyclerFolders.hide()
    }

    private fun handleError() {
        Toast.makeText(binding.root.context, "handleError", Toast.LENGTH_SHORT).show()
    }


}