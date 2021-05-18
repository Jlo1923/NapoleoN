package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.repository

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract.IContractMyMultiAttachmentMsg
import javax.inject.Inject

class MyMultiAttachmentMsgRepository @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
    private val syncManager: SyncManager,
    private val napoleonApi: NapoleonApi,
) : IContractMyMultiAttachmentMsg.Repository {

    override suspend fun getAttachmentsByMessage(messageId: Int): MessageAttachmentRelation? {
        return messageLocalDataSource.getMessageById(messageId, false)
    }

    override suspend fun getAttachmentsByMessageAsLiveData(messageId: Int): LiveData<List<AttachmentEntity>> {
        return messageLocalDataSource.getMessageByIdAsLiveData(messageId, false)
    }

}