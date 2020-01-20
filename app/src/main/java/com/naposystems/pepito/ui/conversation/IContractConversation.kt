package com.naposystems.pepito.ui.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.dto.conversation.message.ConversationReqDTO
import com.naposystems.pepito.dto.conversation.message.ConversationResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.conversation.Conversation
import com.naposystems.pepito.entity.conversation.ConversationAttachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndAttachment
import retrofit2.Response

interface IContractConversation {

    interface ViewModel {
        fun getUser(): User
        fun subscribeToChannel(userToChat: Contact)
        fun unSubscribeToChannel(userToChat: Contact)
        fun getLocalMessages()
        fun getRemoteMessages(channelName: String, contactId: Int)
        fun saveConversationLocally(body: String, quoted: String, contact: Contact, isMine: Int)
        fun saveConversationWithAttachmentLocally(
            body: String,
            quoted: String,
            contact: Contact,
            isMine: Int,
            base64: String,
            uri: String
        )

        fun sendMessage(
            conversationId: Int,
            conversationReqDTO: ConversationReqDTO,
            isMine: Int,
            listAttachmentsId: List<Long>
        )
    }

    interface Repository {
        suspend fun subscribeToChannel(userToChat: Contact): String
        fun unSubscribeToChannel(userToChat: Contact, channelName: String)
        fun getLocalMessages(
            channelName: String,
            pageSize: Int
        ): LiveData<PagedList<ConversationAndAttachment>>

        suspend fun getRemoteMessages(user: User, contactId: Int, channelName: String)
        suspend fun sendMessage(conversationReqDTO: ConversationReqDTO): Response<ConversationResDTO>
        suspend fun getLocalUser(): User
        fun insertConversation(conversation: Conversation): Long
        fun insertListConversation(conversationList: List<Conversation>)
        fun updateConversation(conversation: Conversation)
        fun insertConversationAttachment(listAttachment: List<ConversationAttachment>): List<Long>
        fun updateConversationAttachments(listAttachment: List<ConversationAttachment>)
        fun get422Error(response: Response<ConversationResDTO>): ArrayList<String>
        fun getError(response: Response<ConversationResDTO>): ArrayList<String>
    }
}