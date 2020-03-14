package com.naposystems.pepito.ui.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesReqDTO
import com.naposystems.pepito.dto.conversation.deleteMessages.DeleteMessagesResDTO
import com.naposystems.pepito.dto.conversation.message.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.attachments.MediaStoreAudio
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractConversation {

    interface ViewModel {
        fun getUser(): User
        fun setContact(contact: Contact)
        fun getLocalMessages()
        fun saveMessageLocally(
            body: String,
            quoted: String,
            contact: Contact,
            selfDestructTime: Int,
            isMine: Int
        )

        fun saveMessageWithAttachmentLocally(
            body: String,
            quoted: String,
            contact: Contact,
            isMine: Int,
            base64: String,
            uri: String,
            selfDestructTime: Int,
            origin: Int
        )

        fun saveMessageWithAudioAttachment(
            mediaStoreAudio: MediaStoreAudio,
            selfDestructTime: Int
        )

        fun sendMessage(
            messageId: Int,
            messageReqDTO: MessageReqDTO,
            isMine: Int,
            listAttachmentsId: List<Long>
        )

        fun sendMessagesRead()
        fun getLocalContact(idContact: Int)
        fun updateStateSelectionMessage(idContact: Int, idMessage: Int, isSelected: Boolean)
        fun cleanSelectionMessages(idContact: Int)
        fun deleteMessagesSelected(idContact: Int, listMessages: List<MessageAndAttachment>)
        fun deleteMessagesForAll(idContact: Int, listMessages: List<MessageAndAttachment>)
        fun copyMessagesSelected(idContact: Int)
        fun parsingListByTextBlock(listBody: List<String>): String
        fun getMessagesSelected(idContact: Int)
        fun resetListStringCopy()
        fun setCountOldMessages(count: Int)
        fun getCountOldMessages(): Int
    }

    interface Repository {
        suspend fun subscribeToChannel(userToChat: Contact): String
        fun unSubscribeToChannel(userToChat: Contact, channelName: String)
        fun getLocalMessages(
            contactId: Int,
            pageSize: Int
        ): LiveData<PagedList<MessageAndAttachment>>

        suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        suspend fun getLocalUser(): User
        fun insertMessage(message: Message): Long
        fun insertListMessage(messageList: List<Message>)
        suspend fun insertConversation(messageResDTO: MessageResDTO)
        fun updateMessage(message: Message)
        suspend fun sendMessagesRead(contactId: Int)
        fun insertAttachment(attachment: Attachment): Long
        fun insertAttachments(listAttachment: List<Attachment>): List<Long>
        fun updateAttachments(
            listAttachmentsIds: List<Long>,
            attachments: List<AttachmentResDTO>
        )

        fun get422ErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun get422ErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        fun getLocalContact(idContact: Int): LiveData<Contact>
        suspend fun updateStateSelectionMessage(idContact: Int, idMessage: Int, isSelected: Int)
        suspend fun cleanSelectionMessages(idContact: Int)
        suspend fun deleteMessagesSelected(idContact: Int, listMessages: List<MessageAndAttachment>)
        suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO>
        suspend fun copyMessagesSelected(idContact: Int): List<String>
        suspend fun getMessagesSelected(idContact: Int): LiveData<List<MessageAndAttachment>>

    }
}