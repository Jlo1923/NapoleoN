package com.naposystems.napoleonchat.ui.conversation

import android.net.Uri
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.remote.dto.conversation.call.CallContactResDTO
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
import com.naposystems.napoleonchat.utility.DownloadAttachmentResult
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
        fun saveMessageAndAttachment(
            messageString: String,
            attachmentEntity: AttachmentEntity?,
            numberAttachments: Int,
            selfDestructTime: Int,
            quote: String
        )

        fun saveMessageWithAudioAttachment(
            mediaStoreAudio: MediaStoreAudio,
            selfDestructTime: Int,
            quote: String
        )

        fun sendTextMessagesRead()
        fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Boolean)
        fun cleanSelectionMessages(contactId: Int)
        fun deleteMessagesSelected(contactId: Int, listMessageRelations: List<MessageAttachmentRelation>)
        fun deleteMessagesForAll(contactId: Int, listMessageRelations: List<MessageAttachmentRelation>)
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
        fun uploadAttachment(attachmentEntity: AttachmentEntity, messageEntity: MessageEntity, selfDestructTime: Int)
        fun downloadAttachment(messageAndAttachmentRelation: MessageAttachmentRelation, itemPosition: Int)
        fun updateMessage(messageEntity: MessageEntity)
        fun updateAttachment(attachmentEntity: AttachmentEntity)
        fun sendDocumentAttachment(fileUri: Uri)
        fun resetDocumentCopied()
        fun resetUploadProgress()
        fun sendMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation)
        fun sendMessageRead(messageWebId: String)
        fun reSendMessage(messageEntity: MessageEntity, selfDestructTime: Int)
        fun resetNewMessage()
        fun getFreeTrial(): Long
        fun getMessageNotSent(contactId: Int)
        fun insertMessageNotSent(message: String, contactId: Int)
        fun deleteMessageNotSent(contactId: Int)
    }

    interface Repository {
        fun unSubscribeToChannel(userToChat: ContactEntity, channelName: String)
        fun getLocalMessages(contactId: Int): LiveData<List<MessageAttachmentRelation>>
        suspend fun getQuoteId(quoteWebId: String): Int
        fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAttachmentRelation>
        suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        suspend fun uploadAttachment(attachmentEntity: AttachmentEntity, messageEntity: MessageEntity): Flow<UploadResult>
        suspend fun getLocalUser(): UserEntity
        fun insertMessage(messageEntity: MessageEntity): Long
        fun insertListMessage(messageEntityList: List<MessageEntity>)
        fun updateMessage(messageEntity: MessageEntity)
        suspend fun sendTextMessagesRead(contactId: Int)
        suspend fun sendMissedCallRead(contactId: Int)
        fun insertAttachment(attachmentEntity: AttachmentEntity): Long
        fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long>
        fun updateAttachment(attachmentEntity: AttachmentEntity)
        suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity)
        suspend fun insertQuote(quoteWebId: String, messageEntity: MessageEntity)
        fun get422ErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun get422ErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)
        suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)
        suspend fun cleanSelectionMessages(contactId: Int)
        suspend fun deleteMessagesSelected(contactId: Int, listMessageRelations: List<MessageAttachmentRelation>)
        suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO>
        suspend fun copyMessagesSelected(contactId: Int): List<String>
        suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAttachmentRelation>>
        suspend fun callContact(contact: ContactEntity, isVideoCall: Boolean): Response<CallContactResDTO>
        fun subscribeToCallChannel(channel: String, isVideoCall: Boolean)
        suspend fun downloadAttachment(
            messageAndAttachmentRelation: MessageAttachmentRelation,
            itemPosition: Int
        ): Flow<DownloadAttachmentResult>
        fun updateAttachmentState(messageAndAttachmentRelation: MessageAttachmentRelation, state: Int)
        suspend fun copyFile(fileUri: Uri): File?
        fun verifyMessagesToDelete()
        suspend fun setMessageRead(messageAndAttachmentRelation: MessageAttachmentRelation)
        suspend fun setMessageRead(messageWebId: String)
        suspend fun reSendMessage(messageAndAttachmentRelation: MessageAttachmentRelation)
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