package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractMyMultiAttachmentMsg
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgEvent
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyMultiAttachmentMsgViewModel @Inject constructor(
    private val repository: IContractMyMultiAttachmentMsg.Repository
) : ViewModel(), IContractMyMultiAttachmentMsg.ViewModel, LifecycleObserver {

    private val _state = MutableLiveData<MultiAttachmentMsgState>()
    val state: LiveData<MultiAttachmentMsgState>
        get() = _state

    private val actions: SingleLiveEvent<MultiAttachmentMsgEvent> = SingleLiveEvent()
    fun actions(): LiveData<MultiAttachmentMsgEvent> = actions

    override fun validateStatusAndQuantity(listAttachments: List<AttachmentEntity>) {
        val countSent = listAttachments.filter { it.isSent() }
        if (countSent.size == listAttachments.size) {
            actions.value = MultiAttachmentMsgEvent.HideQuantity
        } else {
            val data = Pair(countSent.size, listAttachments.size)
            actions.value = MultiAttachmentMsgEvent.ShowQuantity(data)
        }
    }

    override fun retryUploadAllFiles() {
        Log.i("JkDev", "retryUploadAllFiles")
    }

    override fun cancelUpload(attachmentEntity: AttachmentEntity) {
        Log.i("JkDev", "cancelUpload")
    }

    override fun retryUpload(attachmentEntity: AttachmentEntity) {
        Log.i("JkDev", "retryUpload")
    }

}