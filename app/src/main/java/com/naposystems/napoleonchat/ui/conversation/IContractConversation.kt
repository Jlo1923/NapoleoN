package com.naposystems.napoleonchat.ui.conversation

import android.net.Uri
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageNotSentEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.ui.conversation.model.ItemMessage
import com.naposystems.napoleonchat.service.download.model.DownloadAttachmentResult
import com.naposystems.napoleonchat.utility.UploadResult
import com.vincent.videocompressor.VideoCompressResult
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

interface IContractConversation {

    interface ViewModel {
        fun getUser(): UserEntity
        fun setContact(contact: ContactEntity)
        fun getLocalMessages()

        fun saveMessageLocally(body: String, selfDestructTime: Int, quote: String)

        /**
         * Save message in local and validate if must send attachment
         *
         * @param itemMessage: get the data for the message to send
         */
        fun saveMessageAndAttachment(itemMessage: ItemMessage)

        fun saveMessageWithAudioAttachment(
            mediaStoreAudio: MediaStoreAudio,
            selfDestructTime: Int,
            quote: String
        )

        fun sendTextMessagesRead()
        fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Boolean)
        fun cleanSelectionMessages(contactId: Int)
        fun deleteMessagesSelected(
            contactId: Int,
            listMessageRelations: List<MessageAttachmentRelation>
        )

        fun deleteMessagesForAll(
            contactId: Int,
            listMessageRelations: List<MessageAttachmentRelation>
        )

        fun deleteMessagesByStatusForMe(contactId: Int, status: Int)
        fun deleteMessagesByStatusForAll(contactId: Int, status: Int)
        fun copyMessagesSelected(contactId: Int)
        fun parsingListByTextBlock(listBody: List<String>): String
        fun getMessagesSelected(contactId: Int)
        fun resetListStringCopy()
        fun setCountOldMessages(count: Int)
        fun getCountOldMessages(): Int
        fun getMessagePosition(messageAndAttachmentRelation: MessageAttachmentRelation): Int
        fun callContact()
        fun resetContactCalledSuccessfully()
        fun resetNoInternetConnection()
        fun setIsVideoCall(isVideoCall: Boolean)
        fun isVideoCall(): Boolean
        fun resetIsVideoCall()
        fun uploadAttachment(
            attachmentEntity: AttachmentEntity,
            messageEntity: MessageEntity,
            selfDestructTime: Int
        )

        fun downloadAttachment(
            messageAndAttachmentRelation: MessageAttachmentRelation,
            itemPosition: Int
        )

        fun updateMessage(messageEntity: MessageEntity)
        fun updateAttachment(attachmentEntity: AttachmentEntity)
        fun sendDocumentAttachment(fileUri: Uri)
        fun resetDocumentCopied()
        fun resetUploadProgress()
        fun sendMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation)
        fun sendMessageRead(messageId: Int, webId: String)
        fun reSendMessage(messageEntity: MessageEntity, selfDestructTime: Int)
        fun resetNewMessage()
        fun getFreeTrial(): Long
        fun getMessageNotSent(contactId: Int)
        fun insertMessageNotSent(message: String, contactId: Int)
        fun deleteMessageNotSent(contactId: Int)
    }

    interface Repository {
        fun getLocalMessages(contactId: Int): LiveData<List<MessageAttachmentRelation>>
        suspend fun getQuoteId(quoteWebId: String): Int
        fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>
        suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        suspend fun uploadAttachment(
            attachmentEntity: AttachmentEntity,
            messageEntity: MessageEntity
        ): Flow<UploadResult>

        suspend fun getLocalUser(): UserEntity
        suspend fun insertMessage(messageEntity: MessageEntity): Long
        fun insertListMessage(messageEntityList: List<MessageEntity>)

        fun updateMessage(
            messageEntity: MessageEntity,
            mustUpdateSelfDestruction: Boolean = true
        )

        suspend fun sendTextMessagesRead(contactId: Int)
        suspend fun sendMissedCallRead(contactId: Int)
        fun insertAttachment(attachmentEntity: AttachmentEntity): Long
        fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long>
        fun updateAttachment(attachmentEntity: AttachmentEntity)
        suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity)
        suspend fun insertQuote(quoteWebId: String, messageEntity: MessageEntity)
        fun getUnprocessableEntityErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun getUnprocessableEntityErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)
        suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)
        suspend fun cleanSelectionMessages(contactId: Int)

        /**
         * Elimina los mensajes seleccionados en la conversacion
         */
        suspend fun deleteMessagesSelected(
            contactId: Int,
            listMessageRelations: List<MessageAttachmentRelation>
        )

        suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO>
        suspend fun copyMessagesSelected(contactId: Int): List<String>
        suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>>

        suspend fun downloadAttachment(
            messageAndAttachmentRelation: MessageAttachmentRelation,
            itemPosition: Int
        ): Flow<DownloadAttachmentResult>

        fun updateAttachmentState(
            messageAndAttachmentRelation: MessageAttachmentRelation,
            state: Int
        )

        suspend fun copyFile(fileUri: Uri): File?
        fun verifyMessagesToDelete()
        suspend fun setMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation)
        suspend fun setMessageRead(messageId: Int, webId: String)
        suspend fun compressVideo(
            attachmentEntity: AttachmentEntity,
            srcFile: File,
            destFile: File,
            job: ProducerScope<*>
        ): Flow<VideoCompressResult>

        fun getFreeTrial(): Long
        fun getMessageNotSent(contactId: Int): MessageNotSentEntity
        fun insertMessageNotSent(message: String, contactId: Int)
        fun deleteMessageNotSent(contactId: Int)
    }
}