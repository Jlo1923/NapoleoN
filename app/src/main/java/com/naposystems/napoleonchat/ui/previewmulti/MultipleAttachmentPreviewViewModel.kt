package com.naposystems.napoleonchat.ui.previewmulti

import androidx.lifecycle.*
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction.SelectItemInTabLayout
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction.ShowSelfDestruction
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import javax.inject.Inject

class MultipleAttachmentPreviewViewModel @Inject constructor(
    private val repository: IContractSelfDestructTime.Repository
) : ViewModel(),
    IContractMultipleAttachmentPreview.ViewModel,
    LifecycleObserver {

    private var isShowingOptions = true
    private var listFiles = mutableListOf<MultipleAttachmentFileItem>()

    private val _state = MutableLiveData<MultipleAttachmentPreviewState>()
    val state: LiveData<MultipleAttachmentPreviewState>
        get() = _state

    private val actions: SingleLiveEvent<MultipleAttachmentPreviewAction> = SingleLiveEvent()
    fun actions(): LiveData<MultipleAttachmentPreviewAction> = actions

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initUi() {
        _state.value = MultipleAttachmentPreviewState.Loading
    }

    fun changeVisibilityOptions() {
        isShowingOptions = isShowingOptions.not()
        if (isShowingOptions) {
            actions.value = MultipleAttachmentPreviewAction.ShowAttachmentOptions
        } else {
            actions.value = MultipleAttachmentPreviewAction.HideAttachmentOptions
        }
    }

    fun defineListFiles(files: ArrayList<MultipleAttachmentFileItem>) {
        listFiles = files
        defineDefaultSelfDestructionTime()
        showFilesAsPager()
    }

    private fun defineDefaultSelfDestructionTime() {
        val selfDestructionTime = repository.getSelfDestructTime()
        listFiles.forEach {
            it.selfDestruction = selfDestructionTime
        }
    }

    private fun showFilesAsPager() {
        _state.value = MultipleAttachmentPreviewState.SuccessFilesAsPager(ArrayList(listFiles))
        validateMustShowTabs()
    }

    fun forceShowOptions() {
        isShowingOptions = true
        actions.value = MultipleAttachmentPreviewAction.ShowAttachmentOptionsWithoutAnim
    }

    fun updateSelfDestructionForItemPosition(
        selectedFileToSee: Int,
        selfDestructTimeSelected: Int
    ) {
        val file = listFiles[selectedFileToSee]
        listFiles.find { it.id == file.id }?.let {
            it.selfDestruction = selfDestructTimeSelected
        }
    }

    fun onDeleteElement(selectedIndexToDelete: Int) {
        removeFileFromListAndShowListInPager(selectedIndexToDelete)
        if (isTheLastFile()) {
            exitPreview()
        } else {
            selectItemInTabLayoutByIndex(selectedIndexToDelete)
        }
    }

    private fun exitPreview() {
        actions.value = MultipleAttachmentPreviewAction.Exit
    }

    private fun isTheLastFile(): Boolean = listFiles.isEmpty()

    private fun validateMustShowTabs() {
        if (listFiles.size == 1) {
            actions.value = MultipleAttachmentPreviewAction.HideFileTabs
        }
    }

    private fun selectItemInTabLayoutByIndex(selectedIndexToDelete: Int) {
        val indexToSelectInTaLayout =
            if (selectedIndexToDelete == 0) 0 else selectedIndexToDelete - 1
        actions.value = SelectItemInTabLayout(indexToSelectInTaLayout)
    }

    private fun removeFileFromListAndShowListInPager(selectedIndexToDelete: Int) {
        val file = listFiles[selectedIndexToDelete]
        listFiles.remove(file)
        showFilesAsPager()
    }

    fun loadSelfDestructionTimeByIndex(position: Int) {
        actions.value =
            ShowSelfDestruction(listFiles[position].selfDestruction)
    }

}