package com.naposystems.pepito.ui.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
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
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractConversation {

    interface ViewModel {
        fun getUser(): User
        fun setContact(contact: Contact)
        fun getLocalMessages()
        fun saveMessageLocally(body: String, selfDestructTime: Int)
        fun saveMessageAndAttachment(
            messageString: String,
            attachment: Attachment?,
            numberAttachments: Int,
            selfDestructTime: Int
        )

        fun saveMessageWithAudioAttachment(mediaStoreAudio: MediaStoreAudio, selfDestructTime: Int)
        fun sendMessagesRead()
        fun getLocalContact(contactId: Int)
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
    }

    interface Repository {
        suspend fun subscribeToChannel(userToChat: Contact): String
        fun unSubscribeToChannel(userToChat: Contact, channelName: String)
        fun getLocalMessages(
            contactId: Int,
            pageSize: Int
        ): LiveData<PagedList<MessageAndAttachment>>
        fun getLocalMessagesByStatus(contactId: Int, status: Int): List<MessageAndAttachment>
        suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        suspend fun sendMessageAttachment(attachment: Attachment): Response<AttachmentResDTO>
        suspend fun getLocalUser(): User
        fun insertMessage(message: Message): Long
        fun insertListMessage(messageList: List<Message>)
        suspend fun insertConversation(messageResDTO: MessageResDTO)
        fun updateMessage(message: Message)
        suspend fun sendMessagesRead(contactId: Int)
        fun insertAttachment(attachment: Attachment): Long
        fun insertAttachments(listAttachment: List<Attachment>): List<Long>
        fun updateAttachment(attachment: Attachment)
        fun get422ErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun getErrorMessage(response: Response<MessageResDTO>): ArrayList<String>
        fun get422ErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        fun getErrorDeleteMessagesForAll(response: ResponseBody): ArrayList<String>
        suspend fun deleteMessagesByStatusForMe(contactId: Int, status: Int)
        fun getLocalContact(contactId: Int): LiveData<Contact>
        suspend fun updateStateSelectionMessage(contactId: Int, idMessage: Int, isSelected: Int)
        suspend fun cleanSelectionMessages(contactId: Int)
        suspend fun deleteMessagesSelected(contactId: Int, listMessages: List<MessageAndAttachment>)
        suspend fun deleteMessagesForAll(deleteMessagesReqDTO: DeleteMessagesReqDTO): Response<DeleteMessagesResDTO>
        suspend fun copyMessagesSelected(contactId: Int): List<String>
        suspend fun getMessagesSelected(contactId: Int): LiveData<List<MessageAndAttachment>>

    }
}