package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract

import android.content.Context
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO

interface IContractMyMultiAttachmentMsg {

    interface ViewModel {

        fun validateStatusAndQuantity(listAttachments: List<AttachmentEntity>)

        fun retryUploadAllFiles(
            attachmentsFilter: List<AttachmentEntity>,
            context: Context,
            id: MessageEntity
        )

        fun cancelUpload(attachmentEntity: AttachmentEntity)

        fun retryUpload(attachmentEntity: AttachmentEntity)

    }

    interface Repository {

        /**
         * get the attachments from Message by Id
         * @param messageId the identifier for the message
         */
        suspend fun getAttachmentsByMessage(messageId: Int): MessageAttachmentRelation?

        suspend fun getAttachmentsByMessageAsLiveData(messageId: Int): LiveData<List<AttachmentEntity>>

        fun notifyMessageWitStatus(listMessagesReceived: MessagesReqDTO)

        suspend fun notifyMessageRead(listMessagesReceived: MessagesReqDTO)

    }
}