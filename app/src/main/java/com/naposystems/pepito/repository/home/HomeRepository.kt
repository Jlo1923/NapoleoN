package com.naposystems.pepito.repository.home

import androidx.lifecycle.LiveData
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.dto.conversation.message.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.dto.conversation.socket.AuthReqDTO
import com.naposystems.pepito.dto.conversation.socket.HeadersReqDTO
import com.naposystems.pepito.dto.conversation.socket.SocketReqDTO
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import com.naposystems.pepito.ui.home.IContractHome
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketService: IContractSocketService.SocketService,
    private val conversationLocalDataSource: ConversationDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val contactLocalDataSource: ContactDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource
) :
    IContractHome.Repository {

    private val firebaseId by lazy {
        sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    override suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO> {
        return napoleonApi.getFriendshipRequestQuantity()
    }

    override suspend fun subscribeToGeneralSocketChannel() {
        var user: User? = null

        coroutineScope {
            user = userLocalDataSource.getUser(firebaseId)
        }

        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        val channelName =
            "private-general.${user!!.id}"

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.subscribe(SocketReqDTO.toJSONObject(socketReqDTO))
    }

    override suspend fun getUser(): User {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userLocalDataSource.getUser(firebaseId)
    }

    override fun getConversations(): LiveData<List<ConversationAndContact>> {
        return conversationLocalDataSource.getConversations()
    }

    override suspend fun getRemoteMessages() {
        val response = napoleonApi.getMyMessages()

        if (response.isSuccessful) {
            val messageResList: MutableList<MessageResDTO> = response.body()!!.toMutableList()

            if (messageResList.isNotEmpty()) {

                for (messageRes in messageResList) {

                    val message = MessageResDTO.toMessageEntity(
                        0, messageRes, Constants.IsMine.NO.value
                    )

                    val conversationId = messageLocalDataSource.insertMessage(message)

                    attachmentLocalDataSource.insertAttachments(
                        AttachmentResDTO.toListConversationAttachment(
                            conversationId.toInt(),
                            messageRes.attachments
                        )
                    )

                    conversationLocalDataSource.insertConversation(
                        messageRes,
                        false,
                        1
                    )
                }
            }
        }
    }

    override suspend fun getContacts() {
        try {

            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                contactLocalDataSource.insertContactList(contacts)
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun getDeletedMessages() {
        try {
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
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }
}