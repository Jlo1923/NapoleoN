package com.naposystems.napoleonchat.ui.multi

import androidx.lifecycle.*
import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentAction
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentAction.ShowSelectFolderName
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFolderItem
import com.naposystems.napoleonchat.ui.multi.views.itemview.MultipleAttachmentFileItemView
import com.naposystems.napoleonchat.ui.multi.views.itemview.MultipleAttachmentPreviewSmallFileItemView
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import com.xwray.groupie.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MAX_FILES = 10

class MultipleAttachmentViewModel @Inject constructor(
    private val repository: IContractMultipleAttachment.Repository
) : ViewModel(),
    IContractMultipleAttachment.ViewModel,
    LifecycleObserver {

    private var isShowingFiles = false
    private var cacheListFolders = emptyList<Item<*>>()
    private var selectedLists = mutableListOf<MultipleAttachmentFileItem>()

    private val _state = MutableLiveData<MultipleAttachmentState>()
    val state: LiveData<MultipleAttachmentState>
        get() = _state

    private val actions: SingleLiveEvent<MultipleAttachmentAction> = SingleLiveEvent()
    fun actions(): LiveData<MultipleAttachmentAction> = actions

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun getFolders() {
        isShowingFiles = false
        viewModelScope.launch {
            try {
                repository.getFolders()
                    .flowOn(Dispatchers.IO)
                    .collect { successFolders(it) }
            } catch (exception: Exception) {
                _state.value = MultipleAttachmentState.Error
            }
        }
    }

    private fun successFolders(it: MultipleAttachmentState) {
        _state.value = it
        if (it is MultipleAttachmentState.SuccessFolders) {
            cacheListFolders = it.listElements
        }
    }

    override fun loadFilesFromFolder(folder: MultipleAttachmentFolderItem) {
        isShowingFiles = true
        viewModelScope.launch {
            try {
                val mapIdsSelected = selectedLists.map { it.id to it.id }.toMap()
                repository.getFilesByFolder(folder.parent, mapIdsSelected)
                    .flowOn(Dispatchers.IO)
                    .collect { successFilesByFolder(it, folderName = folder.folderName) }
            } catch (exception: Exception) {
                _state.value = MultipleAttachmentState.Error
            }
        }
    }

    private fun successFilesByFolder(
        it: MultipleAttachmentState,
        folderName: String
    ) {
        _state.value = it
        if (it is MultipleAttachmentState.SuccessFiles) {
            actions.value = ShowSelectFolderName(folderName)
        }
    }

    fun handleBackAction() {
        if (isShowingFiles) {
            actions.value = MultipleAttachmentAction.BackToFolderList
            _state.value = MultipleAttachmentState.SuccessFolders(cacheListFolders)
            isShowingFiles = false
        } else {
            actions.value = MultipleAttachmentAction.Exit
        }
    }

    override fun addFileToList(item: MultipleAttachmentFileItem) {
        selectedLists.add(item)
        showPreviewSelectedFiles()
    }

    override fun removeFileToList(item: MultipleAttachmentFileItem) {
        selectedLists.remove(item)
        if (selectedLists.isEmpty()) {
            actions.value = MultipleAttachmentAction.HideListSelectedFiles
        } else {
            showPreviewSelectedFiles()
        }
    }

    private fun showPreviewSelectedFiles() {
        val previewItems = selectedLists.map { MultipleAttachmentPreviewSmallFileItemView(it) }
        actions.value = MultipleAttachmentAction.ShowPreviewSelectedFiles(previewItems)
    }

    fun tryAddToListAttachments(item: MultipleAttachmentFileItemView) {
        if (item.isSelected.not()) {
            if (selectedLists.size < MAX_FILES) {
                selectedLists.add(item.item)
                item.isSelected = item.isSelected.not()
                showPreviewSelectedFiles()
            } else {
                actions.value = MultipleAttachmentAction.ShowHasMaxFilesAttached
            }
        } else {
            selectedLists.remove(item.item)
            item.isSelected = item.isSelected.not()
            showPreviewSelectedFiles()
        }
    }

    fun continueToPreview() {
        actions.value = MultipleAttachmentAction.ContinueToPreview(selectedLists)
    }

}