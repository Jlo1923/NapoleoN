package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.repository

import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.IContractMyMultiAttachmentMsg
import javax.inject.Inject

class MyMultiAttachmentMsgRepository @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
) : IContractMyMultiAttachmentMsg.Repository {

    override suspend fun getAttachmentsByMessage(messageId: Int): MessageAttachmentRelation? {
        return messageLocalDataSource.getMessageById(messageId, false)
    }

}