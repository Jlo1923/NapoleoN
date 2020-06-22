package com.naposystems.pepito.ui.conversation

import android.net.Uri
import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.conversation.call.CallContactResDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import com.naposystems.pepito.utility.DownloadAttachmentResult
import com.naposystems.pepito.utility.UploadResult
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File

interface IContractConversation {

    interface ViewModel {
        fun getUser(): User
        fun setContact(contact: Contact)
        fun getLocalMessages()

        fun saveMessageLocally(body: String, selfDestructTime: Int, quote: String)
        fun saveMessageAndAttachment(
            messageString: String,
            attachment: Attachment?,
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
        fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>)
        fun deleteMessagesForAll(contactId: Int, listMessages: List<MessageAndAttachment>)
        fun deleteMessagesByStatusForMe(contactId: Int, status: Int)
        fun deleteMessagesByStatusForAll(contactId: Int, status: Int)
        fun copyMessagesSelected(contactId: Int)
        fun parsingListByTextBlock(listBody: List<String>): String
        fun getMessagesSelected(contactId: Int)
        fun resetListStringCopy()
        fun setCountOldMessages(count: Int)
        fun getCountOldMessages(): Int
        fun getMessagePosition(messageAndAttachment: MessageAndAttachment): Int
        fun callContact()
        fun resetContactCalledSuccessfully()
        fun setIsVideoCall(isVideoCall: Boolean)
        fun isVideoCall(): Boolean
        fun resetIsVideoCall()
        fun uploadAttachment(attachment: Attachment, message: Message, selfDestructTime: Int)
        fun downloadAttachment(messageAndAttachment: MessageAndAttachment, itemPosition: Int)
        fun updateMessage(message: Message)
        fun updateAttachment(attachment: Attachment)
        fun sendDocumentAttachment(fileUri: Uri)
        fun resetDocumentCopied()
        fun resetUploadProgress()
        fun sendMessageRead(messageAndAttachment: MessageAndAttachment)
        fun reSendMessage(message: Message, selfDestructTime: Int)
    }

    interface Repository {
        suspend fun subscribeToChannel(userToChat: Contact): String
        fun unSubscribeToChannel(userToChat: Contact, channelName: String)
        fun getLocalMessages(contactId: Int): LiveData<List<MessageAndAttachment>>
        suspend fun getQuoteId(quoteWebId: String): Int
        fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment>
        suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        suspend fun uploadAttachment(attachment: Attachment, message: Message): Flow<UploadResult>
        suspend fun getLocalUser(): User
        fun insertMessage(message: Message): Long
        fun insertListMessage(messageList: List<Message>)
        fun updateMessage(message: Message)
        suspend fun sendTextMessagesRead(contactId: Int)
        fun insertAttachment(attachment: Attachment): Long
        fun insertAttachments(listAttachment: List<Attachment>): List<Long>
        fun updateAttachment(attachment: Attachment)
        suspend fun suspendUpdateAttachment(attachment: Attachment)
        suspend fun insertQuote(quoteWebId: String, message: Message)
        fun get422ErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun get422ErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)
        suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)
        suspend fun cleanSelectionMessages(contactId: Int)
        suspend fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>)
        suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO>
        suspend fun copyMessagesSelected(contactId: Int): List<String>
        suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>>
        suspend fun callContact(contact: Contact, isVideoCall: Boolean): Response<CallContactResDTO>
        fun subscribeToCallChannel(channel: String)
        suspend fun downloadAttachment(
            messageAndAttachment: MessageAndAttachment,
            itemPosition: Int
        ): Flow<DownloadAttachmentResult>

        fun updateAttachmentState(messageAndAttachment: MessageAndAttachment, state: Int)
        suspend fun copyFile(fileUri: Uri): File?
        fun verifyMessagesToDelete()
        suspend fun setMessageRead(messageAndAttachment: MessageAndAttachment)
        suspend fun reSendMessage(messageAndAttachment: MessageAndAttachment)
    }
}