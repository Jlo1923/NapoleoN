package com.naposystems.napoleonchat.repository.notificationUtils

import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageReceivedReqDTO
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.dto.validateMessageEvent.ValidateMessage
import com.naposystems.napoleonchat.dto.validateMessageEvent.ValidateMessageEventDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.message.Quote
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.naposystems.napoleonchat.webService.socket.IContractSocketService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class NotificationUtilsRepository @Inject constructor(
    private val cryptoMessage: CryptoMessage,
    private val napoleonApi: NapoleonApi,
    private val socketService: IContractSocketService.SocketService,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val quoteDataSource: QuoteDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractNotificationUtils.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    private suspend fun getRemoteContact() {
        try {
            val response = napoleonApi.getContactsByState(Constants.FriendShipState.ACTIVE.state)

            if (response.isSuccessful) {

                val contactResDTO = response.body()!!

                val contacts = ContactResDTO.toEntityList(contactResDTO.contacts)

                val contactsToDelete = contactLocalDataSource.insertOrUpdateContactList(contacts)

                if (contactsToDelete.isNotEmpty()) {

                    contactsToDelete.forEach { contact ->
                        messageLocalDataSource.deleteMessageByType(
                            contact.id,
                            Constants.MessageType.NEW_CONTACT.type
                        )

                        RxBus.publish(RxEvent.DeleteChannel(contact))

                        contactLocalDataSource.deleteContact(contact)
                    }
                }
            } else {
                Timber.e(response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun insertMessage(messageString: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val newMessageEventMessageResData: String = if (BuildConfig.ENCRYPT_API) {
                cryptoMessage.decryptMessageBody(messageString)
            } else {
                messageString
            }

            val jsonAdapter: JsonAdapter<NewMessageEventMessageRes> =
                moshi.adapter(NewMessageEventMessageRes::class.java)

            jsonAdapter.fromJson(newMessageEventMessageResData)?.let { newMessageEventMessageRes ->
                Timber.d("*MessageDataTest: $newMessageEventMessageRes")

                if (newMessageEventMessageRes.messageType == Constants.MessageType.NEW_CONTACT.type) {
                    getRemoteContact()
                }

                validateMessageEvent(newMessageEventMessageRes)

                val databaseMessage =
                    messageLocalDataSource.getMessageByWebId(newMessageEventMessageRes.id, false)

                if (databaseMessage == null) {

                    val message =
                        newMessageEventMessageRes.toMessageEntity(Constants.IsMine.NO.value)

//                    if (BuildConfig.ENCRYPT_API) {
//                        message.encryptBody(cryptoMessage)
//                    }

                    val messageId =
                        messageLocalDataSource.insertMessage(message)
                    Timber.d("Conversation insertó mensajes")

                    if (newMessageEventMessageRes.quoted.isNotEmpty()) {
                        insertQuote(newMessageEventMessageRes.quoted, messageId.toInt())
                    }

                    val listAttachments =
                        NewMessageEventAttachmentRes.toListConversationAttachment(
                            messageId.toInt(),
                            newMessageEventMessageRes.attachments
                        )

                    attachmentLocalDataSource.insertAttachments(listAttachments)
                    Timber.d("Conversation insertó attachment")
                }
            }
        }
    }

    private fun validateMessageEvent(newMessageDataEventRes: NewMessageEventMessageRes) {
        try {
            val messages = arrayListOf(
                ValidateMessage(
                    id = newMessageDataEventRes.id,
                    user = newMessageDataEventRes.userAddressee,
                    status = Constants.MessageEventType.UNREAD.status
                )
            )

            val validateMessage = ValidateMessageEventDTO(messages)

            val jsonAdapterValidate =
                moshi.adapter(ValidateMessageEventDTO::class.java)

            val json = jsonAdapterValidate.toJson(validateMessage)

            socketService.emitToClientConversation(json.toString())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun notifyMessageReceived(messageId: String) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val messageReceivedReqDTO = MessageReceivedReqDTO(messageId)
                    napoleonApi.notifyMessageReceived(messageReceivedReqDTO)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    override fun getIsOnCallPref() = Data.isOnCall

    override fun getContactSilenced(contactId: Int, silenced: (Boolean?) -> Unit) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                silenced(contactLocalDataSource.getContactSilenced(contactId))
            }
        }
    }

    override fun getContact(contactId: Int): Contact? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun getNotificationChannelCreated(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_CHANNEL_CREATED)
    }

    override fun setNotificationChannelCreated() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_CHANNEL_CREATED,
            Constants.ChannelCreated.TRUE.state
        )
    }

    override fun getNotificationMessageChannelId(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID
        )
    }

    override fun setNotificationMessageChannelId(newId: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_NOTIFICATION_MESSAGE_CHANNEL_ID,
            newId
        )
    }

    override fun getCustomNotificationChannelId(contactId: Int): String? {
        val contact = contactLocalDataSource.getContactById(contactId)
        return contact?.notificationId
    }

    override fun setCustomNotificationChannelId(contactId: Int, newId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            contactLocalDataSource.updateChannelId(contactId, newId)
        }
    }

    override fun getContactById(contactId: Int): Contact? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun updateStateChannel(contactId: Int, state: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            contactLocalDataSource.updateStateChannel(contactId, state)
        }
    }

    private suspend fun insertQuote(quoteWebId: String, messageId: Int) {
        val originalMessage =
            messageLocalDataSource.getMessageByWebId(quoteWebId, false)

        if (originalMessage != null) {
            var firstAttachment: Attachment? = null

            if (originalMessage.attachmentList.isNotEmpty()) {
                firstAttachment = originalMessage.attachmentList.first()
            }

            val quote = Quote(
                id = 0,
                messageId = messageId,
                contactId = originalMessage.message.contactId,
                body = originalMessage.message.body,
                attachmentType = firstAttachment?.type ?: "",
                thumbnailUri = firstAttachment?.fileName ?: "",
                messageParentId = originalMessage.message.id,
                isMine = originalMessage.message.isMine
            )

            quoteDataSource.insertQuote(quote)
        }
    }
}
