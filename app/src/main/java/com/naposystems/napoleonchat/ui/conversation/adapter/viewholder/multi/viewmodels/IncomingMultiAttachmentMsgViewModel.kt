package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import com.naposystems.napoleonchat.service.download.DownloadAttachmentsService
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractIncomingMultiAttachmentMsg
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractMyMultiAttachmentMsg
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgEvent
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.events.MultiAttachmentMsgState
import com.naposystems.napoleonchat.utility.SingleLiveEvent
import javax.inject.Inject

class IncomingMultiAttachmentMsgViewModel @Inject constructor(
    private val repository: IContractMyMultiAttachmentMsg.Repository
) : ViewModel(), IContractIncomingMultiAttachmentMsg.ViewModel, LifecycleObserver {

    private val _state = MutableLiveData<MultiAttachmentMsgState>()
    val state: LiveData<MultiAttachmentMsgState>
        get() = _state

    private val actions: SingleLiveEvent<MultiAttachmentMsgEvent> = SingleLiveEvent()
    fun actions(): LiveData<MultiAttachmentMsgEvent> = actions

    override fun validateStatusAndQuantity(listAttachments: List<AttachmentEntity>) {
        val countSent = listAttachments.filter { it.isDownloaded() }
        if (countSent.size == listAttachments.size) {
            actions.value = MultiAttachmentMsgEvent.HideQuantity
        } else {
            val data = Pair(countSent.size, listAttachments.size)
            actions.value = MultiAttachmentMsgEvent.ShowQuantity(data)
        }
    }

    override fun retryDownloadAllFiles(
        attachmentsFilter: List<AttachmentEntity>,
        context: Context
    ) {
        val intent = Intent(context, DownloadAttachmentsService::class.java).apply {
            putExtras(Bundle().apply {
                putParcelableArrayList(
                    MultipleUploadService.ATTACHMENT_KEY,
                    ArrayList(attachmentsFilter)
                )
            })
        }
        context.startService(intent)
    }

    override fun cancelDownload(attachmentEntity: AttachmentEntity) {
        Log.i("JkDev", "cancelDownload ${attachmentEntity.id}")
    }

    override fun retryDownload(attachmentEntity: AttachmentEntity, context: Context) {
        val intent = Intent(context, DownloadAttachmentsService::class.java).apply {
            putExtras(Bundle().apply {
                putParcelableArrayList(
                    MultipleUploadService.ATTACHMENT_KEY,
                    ArrayList(listOf(attachmentEntity))
                )
            })
        }
        //context.startService(intent)
    }


}