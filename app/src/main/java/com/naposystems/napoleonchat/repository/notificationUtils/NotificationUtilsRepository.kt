package com.naposystems.napoleonchat.repository.notificationUtils

import android.content.Context
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDataSource
import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.db.dao.message.MessageDataSource
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.napoleonchat.dto.conversation.message.MessageReceivedReqDTO
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventAttachmentRes
import com.naposystems.napoleonchat.dto.newMessageEvent.NewMessageEventMessageRes
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.message.Quote
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Data
import com.naposystems.napoleonchat.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class NotificationUtilsRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val messageLocalDataSource: MessageDataSource,
    private val quoteDataSource: QuoteDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource
) :
    IContractNotificationUtils.Repository {


    override fun insertMessage(newMessageEventMessageRes: NewMessageEventMessageRes) {
        GlobalScope.launch(Dispatchers.IO) {
            val databaseMessage =
                messageLocalDataSource.getMessageByWebId(newMessageEventMessageRes.id, false)

            if (databaseMessage == null) {

                val message = newMessageEventMessageRes.toMessageEntity(Constants.IsMine.NO.value)

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
            }
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