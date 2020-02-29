package com.naposystems.pepito.repository.socket

import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.dto.conversation.message.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SocketRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val conversationLocalDataSource: ConversationDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource
) : IContractSocketService.Repository {

    override fun getMyMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getMyMessages()

            if (response.isSuccessful) {
                val messageResList: MutableList<MessageResDTO> = response.body()!!.toMutableList()

                if (messageResList.isNotEmpty()) {

                    for (messageRes in messageResList) {

                        val message = MessageResDTO.toMessageEntity(
                            0, messageRes, Constants.IsMine.NO.value
                        )

                        val conversationId = messageLocalDataSource.insertMessage(message)

                        attachmentLocalDataSource.insertAttachment(
                            AttachmentResDTO.toListConversationAttachment(
                                conversationId.toInt(),
                                messageRes.attachments
                            )
                        )

                        val unreadMessages =
                            messageResList.filter { it.userDestination == messageRes.userDestination }.size

                        conversationLocalDataSource.insertConversation(
                            messageRes,
                            false,
                            unreadMessages
                        )
                    }

                }
            }
        }
    }

    override fun verifyMessagesReceived() {
        GlobalScope.launch {
            val response = napoleonApi.verifyMessagesReceived()

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.UNREAD.status
                )
            }
        }
    }

    override fun verifyMessagesRead() {
        GlobalScope.launch {
            val response = napoleonApi.verifyMessagesRead()

            if (response.isSuccessful) {
                messageLocalDataSource.updateMessageStatus(
                    response.body()!!,
                    Constants.MessageStatus.READED.status
                )
            }
        }
    }

    override fun getDeletedMessages() {
        GlobalScope.launch {
            val response = napoleonApi.getDeletedMessages()
            if(response.isSuccessful) {
                val idContact = messageLocalDataSource.getIdContactWithWebId(response.body()!!)
                messageLocalDataSource.deletedMessages(response.body()!!)
                when(val messageAndAttachment=  messageLocalDataSource.getLastMessageByContact(idContact)) {
                    null -> {
                        conversationLocalDataSource.cleanConversation(idContact)
                    }
                    else -> {
                        conversationLocalDataSource.getQuantityUnreads(idContact).let { quantityUnreads->
                            if (quantityUnreads > 0) {
                                conversationLocalDataSource.updateConversationByContact(
                                    idContact,
                                    messageAndAttachment.message.body,
                                    messageAndAttachment.message.createdAt,
                                    messageAndAttachment.message.status,
                                    quantityUnreads - response.body()!!.count())
                            } else {
                                conversationLocalDataSource.updateConversationByContact(
                                    idContact,
                                    messageAndAttachment.message.body,
                                    messageAndAttachment.message.createdAt,
                                    messageAndAttachment.message.status,
                                    0)
                            }
                        }
                    }
                }
            }
        }
    }
}