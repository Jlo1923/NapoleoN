package com.naposystems.napoleonchat.ui.previewmulti.contract

import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem


interface IContractMultipleAttachmentPreview {

    interface ViewModel {

    }

    interface Repository {

        /**
         * Insert the message in database local and return the ID
         *
         * @param message: the message to insert
         * @return new identifier for the message
         */
        suspend fun insertMessageToContact(message: ItemMessage): MessageEntity

        /**
         * Delete the messages not send to contact for the user
         *
         * @param id contact ID
         */
        fun deleteMessageNotSent(id: Int)

        /**
         * Insert the attachments in the list with message Id
         *
         * @param listFiles the files to insert as attachments
         * @param messageId the parent for the attachments
         */
        suspend fun insertAttachmentsWithMsgId(
            listFiles: MutableList<MultipleAttachmentFileItem>,
            messageId: Int
        ): List<AttachmentEntity?>

        /**
         *
         */
        suspend fun sendMessage(messageEntity: MessageEntity): Pair<MessageEntity?, String>?

    }


}