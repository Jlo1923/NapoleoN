package com.naposystems.napoleonchat.ui.previewmulti

import androidx.lifecycle.*
import com.naposystems.napoleonchat.ui.multi.events.MultipleAttachmentState
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.previewmulti.contract.IContractMultipleAttachmentPreview
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewState
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class MultipleAttachmentPreviewViewModel @Inject constructor(
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

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
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
    }

}