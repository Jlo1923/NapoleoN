package com.naposystems.pepito.repository.conversation

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.conversation.ConversationDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.conversation.message.*
import com.naposystems.pepito.dto.conversation.socket.AuthReqDTO
import com.naposystems.pepito.dto.conversation.socket.HeadersReqDTO
import com.naposystems.pepito.dto.conversation.socket.SocketReqDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.message.Message
import com.naposystems.pepito.entity.message.Attachment
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.ui.conversation.IContractConversation
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.WebServiceUtils
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import com.squareup.moshi.Moshi
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ConversationRepository @Inject constructor(
    private val socketService: IContractSocketService.SocketService,
    private val userLocalDataSource: UserLocalDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi,
    private val conversationLocalDataSource: ConversationDataSource,
    private val contactDataSource: ContactDataSource
) :
    IContractConversation.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private val firebaseId: String by lazy {
        sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    override suspend fun subscribeToChannel(userToChat: Contact): String {

        var user: User? = null

        coroutineScope {
            user = getLocalUser()
        }

        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        var minorId: String = userToChat.id.toString()
        var mayorId: String = user!!.id.toString()

        if (user!!.id < userToChat.id) {
            mayorId = userToChat.id.toString()
            minorId = user!!.id.toString()
        }

        val channelName =
            "private-private.${minorId}.${mayorId}"

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.subscribe(SocketReqDTO.toJSONObject(socketReqDTO))

        return channelName
    }

    override fun unSubscribeToChannel(userToChat: Contact, channelName: String) {
        val headersReqDTO = HeadersReqDTO(
            firebaseId
        )

        val authReqDTO = AuthReqDTO(
            headersReqDTO
        )

        val socketReqDTO = SocketReqDTO(
            channelName,
            authReqDTO
        )

        socketService.unSubscribe(SocketReqDTO.toJSONObject(socketReqDTO), channelName)
    }

    override fun getLocalMessages(
        contactId: Int,
        pageSize: Int
    ): LiveData<PagedList<MessageAndAttachment>> {
        return messageLocalDataSource.getMessages(contactId, pageSize)
    }

    override suspend fun sendMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO> {
        return napoleonApi.sendMessage(messageReqDTO)
    }

    override fun getLocalContact(idContact: Int): LiveData<Contact> {
        return contactDataSource.getContact(idContact)
    }

    override suspend fun getLocalUser(): User {
        return userLocalDataSource.getUser(firebaseId)
    }

    override fun insertMessage(message: Message): Long {
        return messageLocalDataSource.insertMessage(message)
    }

    override fun insertListMessage(messageList: List<Message>) {
        messageLocalDataSource.insertListMessage(messageList)
    }

    override suspend fun insertConversation(messageResDTO: MessageResDTO) {
        conversationLocalDataSource.insertConversation(messageResDTO, true, 0)
    }

    override fun updateMessage(message: Message) {
        messageLocalDataSource.updateMessage(message)
    }

    override suspend fun sendMessagesRead(contactId: Int) {
        val messagesUnread =
            messageLocalDataSource.getMessagesByStatus(Constants.MessageStatus.UNREAD.status)

        if (messagesUnread.isNotEmpty()) {
            try {

                val messagesReadReqDTO = MessagesReadReqDTO(
                    messagesUnread
                )

                val response = napoleonApi.sendMessagesRead(messagesReadReqDTO)

                if (response.isSuccessful) {
                    messageLocalDataSource.updateMessageStatus(
                        response.body()!!,
                        Constants.MessageStatus.READED.status
                    )

                    conversationLocalDataSource.updateConversation(contactId)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    override fun insertAttachment(listAttachment: List<Attachment>): List<Long> {
        return attachmentLocalDataSource.insertAttachment(listAttachment)
    }

    override fun updateAttachments(
        listAttachmentsIds: List<Long>,
        attachments: List<AttachmentResDTO>
    ) {
        for ((index, attachmentResDTO) in attachments.withIndex()) {
            attachmentLocalDataSource.updateAttachments(
                listAttachmentsIds[index],
                attachmentResDTO.id,
                attachmentResDTO.messageId,
                attachmentResDTO.body
            )
        }
    }

    override fun get422Error(response: Response<MessageResDTO>): ArrayList<String> {
        val adapter = moshi.adapter(Message422DTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        return WebServiceUtils.get422Errors(conversationError!!)
    }

    override fun getError(response: Response<MessageResDTO>): ArrayList<String> {

        val adapter = moshi.adapter(MessageErrorDTO::class.java)

        val conversationError = adapter.fromJson(response.errorBody()!!.string())

        val errorList = ArrayList<String>()

        errorList.add(conversationError!!.error)

        return errorList
    }
}