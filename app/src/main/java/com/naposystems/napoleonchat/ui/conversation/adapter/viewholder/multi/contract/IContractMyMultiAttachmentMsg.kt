package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi.contract

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation

interface IContractMyMultiAttachmentMsg {

    interface ViewModel {

        fun retryUploadAllFiles()

    }

    interface Repository {

        /**
         * get the attachments from Message by Id
         * @param messageId the identifier for the message
         */
        suspend fun getAttachmentsByMessage(messageId: Int): MessageAttachmentRelation?

        suspend fun getAttachmentsByMessageAsLiveData(messageId: Int): LiveData<List<AttachmentEntity>>

    }
}