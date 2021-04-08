package com.naposystems.napoleonchat.ui.conversation.adapter.viewholder.multi

import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation

interface IContractMyMultiAttachmentMsg {

    interface ViewModel {

        fun getAttachmentsInMessage(id: Int)

        fun retryUploadAllFiles()

    }

    interface Repository {

        /**
         * get the attachments from Message by Id
         * @param messageId the identifier for the message
         */
        suspend fun getAttachmentsByMessage(messageId: Int): MessageAttachmentRelation?

    }
}