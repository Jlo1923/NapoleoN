package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractMyMultiAttachmentMsg
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgEvent
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.utility.SingleLiveEvent
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

    override fun retryUploadAllFiles(
        attachments: List<AttachmentEntity>,
        context: Context,
        messageEntity: MessageEntity
    ) {
        val intent = Intent(context, MultipleUploadService::class.java).apply {
            putExtras(Bundle().apply {
                putParcelable(MultipleUploadService.MESSAGE_KEY, messageEntity)
                putParcelableArrayList(
                    MultipleUploadService.ATTACHMENT_KEY,
                    ArrayList(attachments)
                )
            })
        }
        context.startService(intent)
    }

    override fun cancelUpload(attachmentEntity: AttachmentEntity) {
        Log.i("JkDev", "cancelUpload")
    }

    override fun retryUpload(attachmentEntity: AttachmentEntity) {
        Log.i("JkDev", "retryUpload")
    }

}