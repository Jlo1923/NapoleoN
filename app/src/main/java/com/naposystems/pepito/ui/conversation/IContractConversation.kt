package com.naposystems.pepito.ui.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.dto.conversation.message.MessageReqDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.Attachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
import retrofit2.Response

interface IContractConversation {

    interface ViewModel {
        fun getUser(): User
        fun setContact(contact: Contact)
        fun getLocalMessages()
        fun saveMessageLocally(body: String, quoted: String, contact: Contact, isMine: Int)
        fun saveMessageWithAttachmentLocally(
            body: String,
            quoted: String,
            contact: Contact,
            isMine: Int,
            base64: String,
            uri: String
        )

        fun sendMessage(
            messageId: Int,
            messageReqDTO: MessageReqDTO,
            isMine: Int,
            listAttachmentsId: List<Long>
        )
        fun sendMessagesRead()
        fun getLocalContact(idContact : Int)
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
        fun insertAttachment(listAttachment: List<Attachment>): List<Long>
        fun updateAttachments(listAttachment: List<Attachment>)
        fun get422Error(response: Response<MessageResDTO>): ArrayList<String>
        fun getError(response: Response<MessageResDTO>): ArrayList<String>
        fun getLocalContact(idContact : Int): LiveData<Contact>
    }
}