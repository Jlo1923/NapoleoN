package com.naposystems.napoleonchat.ui.multi

import androidx.lifecycle.*
import com.naposystems.napoleonchat.ui.multi.contract.IContractMultipleAttachment
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentAction
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentAction.ShowListSelectedFiles
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import com.xwray.groupie.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class MultipleAttachmentViewModel @Inject constructor(
    private val repository: IContractMultipleAttachment.Repository
) : ViewModel(), IContractMultipleAttachment.ViewModel, LifecycleObserver {

    private var isShowingFiles = false
    private var cacheListFolders = emptyList<Item<*>>()

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
                    .collect {
                        _state.value = it
                        if (it is MultipleAttachmentState.SuccessFolders) {
                            cacheListFolders = it.listElements
                        }
                    }
            } catch (exception: Exception) {
                _state.value = MultipleAttachmentState.Error
            }
        }
    }


    fun loadFilesFromFolder(folderName: String) {
        isShowingFiles = true
        viewModelScope.launch {
            try {
                repository.getFilesByFolder(folderName)
                    .flowOn(Dispatchers.IO)
                    .collect {
                        _state.value = it
                        if (it is MultipleAttachmentState.SuccessFiles) {
                            actions.value = ShowListSelectedFiles(folderName)
                        }
                    }
            } catch (exception: Exception) {
                _state.value = MultipleAttachmentState.Error
            }
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
    }

    override fun removeFileToList(item: MultipleAttachmentFileItem) {
    }

}