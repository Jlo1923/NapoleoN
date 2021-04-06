package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi

import androidx.lifecycle.*
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgAction
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.ui.previewmulti.events.MultipleAttachmentPreviewAction
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyMultiAttachmentMsgViewModel @Inject constructor(
    private val repository: IContractMyMultiAttachmentMsg.Repository
) : ViewModel(), IContractMyMultiAttachmentMsg.ViewModel, LifecycleObserver {

    private lateinit var _messageMessagesRelation: LiveData<List<MessageAttachmentRelation>>
    val messageMessagesRelation: LiveData<List<MessageAttachmentRelation>>
        get() = _messageMessagesRelation

    private val _state = MutableLiveData<MultiAttachmentMsgState>()
    val state: LiveData<MultiAttachmentMsgState>
        get() = _state

    private val actions: SingleLiveEvent<MultiAttachmentMsgAction> = SingleLiveEvent()
    fun actions(): LiveData<MultiAttachmentMsgAction> = actions

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun getAttachmentsInMessage(messageId: Int) {
        viewModelScope.launch {
            try {
                val msgAndAttachments = repository.getAttachmentsByMessage(messageId)
                msgAndAttachments?.attachmentEntityList?.let {
                    _state.value = when (it.size) {
                        2 -> MultiAttachmentMsgState.ShowTwoItem(it)
                        3 -> MultiAttachmentMsgState.ShowThreeItem(it)
                        4 -> MultiAttachmentMsgState.ShowFourItem(it)
                        5 -> MultiAttachmentMsgState.ShowFiveItem(it)
                        else -> MultiAttachmentMsgState.ShowMoreItem(it)
                    }
                    validateStatusAndQuantity(it)
                }
            } catch (exception: Exception) {
                //_state.value = MultipleAttachmentState.Error
            }
        }

    }

    private fun validateStatusAndQuantity(listAttachments: List<AttachmentEntity>) {
        val countSent = listAttachments.filter { it.isSent() }
        val data = Pair(countSent.size, listAttachments.size)
        actions.value = MultiAttachmentMsgAction.ShowQuantity(data)
    }

    override fun retryUploadAllFiles() {
    }

    fun cancelDownload(attachmentEntity: AttachmentEntity) {

    }

    fun cancelUpload(attachmentEntity: AttachmentEntity) {

    }

    fun retryDownload(attachmentEntity: AttachmentEntity) {

    }

    fun retryUpload(attachmentEntity: AttachmentEntity) {

    }

}