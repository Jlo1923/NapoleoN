package com.naposystems.pepito.db.dao.conversation

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.entity.conversation.Conversation
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import javax.inject.Inject

class ConversationLocalDataSource @Inject constructor(private val conversationDao: ConversationDao) :
    ConversationDataSource {

    override suspend fun insertConversations(
        messagesResList: List<MessageResDTO>,
        isMine: Boolean
    ) {
        val mutableList = messagesResList.toMutableList()

        mutableList.sortBy { it.userDestination }

        for (messageRes in mutableList) {

            val unreadMessages =
                mutableList.filter { it.userDestination == messageRes.userDestination }.size

            insertOrUpdateConversation(messageRes, unreadMessages, isMine)
        }
    }

    override suspend fun insertConversation(
        messageRes: MessageResDTO,
        isMine: Boolean,
        unreadMessages: Int
    ) {
        insertOrUpdateConversation(messageRes, unreadMessages, isMine)
    }

    private suspend fun insertOrUpdateConversation(
        messageRes: MessageResDTO,
        unreadMessages: Int,
        isMine: Boolean
    ) {

        val contactId = if (isMine) messageRes.userDestination else messageRes.userAddressee

        val conversations = getConversationByContactId(contactId)

        if (conversations.isEmpty()) {

            val conversation =
                Conversation(
                    0,
                    contactId,
                    messageRes.body,
                    messageRes.createdAt,
                    0,
                    unreadMessages
                )

            conversationDao.insertConversation(conversation)

        } else {

            val conversation = conversations[0]
            conversation.createdAt = messageRes.createdAt
            conversation.message = messageRes.body
            conversation.unReads = if (isMine) 0 else conversation.unReads + unreadMessages
            conversationDao.updateConversation(conversation)
        }
    }

    private suspend fun getConversationByContactId(contactId: Int): List<Conversation> {
        return conversationDao.getConversationByContactId(contactId)
    }

    override fun getConversations(): LiveData<List<ConversationAndContact>> {
        return conversationDao.getConversations()
    }

    override suspend fun updateConversation(contactId: Int) {
        val conversations = getConversationByContactId(contactId)

        val conversation = conversations[0]
        conversation.unReads = 0
        conversationDao.updateConversation(conversation)
    }

    override suspend fun cleanConversation(contactId: Int) {
        conversationDao.cleanConversation(contactId)
    }

    override suspend fun deleteConversationAndMessages(contactId: Int) {
        conversationDao.deleteConversation(contactId)
    }
}