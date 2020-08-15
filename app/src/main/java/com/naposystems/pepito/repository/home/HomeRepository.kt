package com.naposystems.pepito.repository.home

import android.content.Context
import androidx.lifecycle.LiveData
import com.naposystems.pepito.BuildConfig
import com.naposystems.pepito.crypto.message.CryptoMessage
import com.naposystems.pepito.db.dao.attachment.AttachmentDataSource
import com.naposystems.pepito.db.dao.contact.ContactDataSource
import com.naposystems.pepito.db.dao.message.MessageDataSource
import com.naposystems.pepito.db.dao.quoteMessage.QuoteDataSource
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.pepito.dto.conversation.message.MessageResDTO
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.entity.message.Quote
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.home.IContractHome
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.naposystems.pepito.webService.socket.IContractSocketService
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketService: IContractSocketService.SocketService,
    private val messageLocalDataSource: MessageDataSource,
    private val contactLocalDataSource: ContactDataSource,
    private val attachmentLocalDataSource: AttachmentDataSource,
    private val quoteDataSource: QuoteDataSource
) :
    IContractHome.Repository {

    private val cryptoMessage = CryptoMessage(context)

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

        val channelName =
            "private-general.${user!!.id}"

        socketService.subscribe(channelName)
    }

    override suspend fun getUserLiveData(): LiveData<User> {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userLocalDataSource.getUserLiveData(firebaseId)
    }

    override suspend fun getRemoteMessages() {
        val response = napoleonApi.getMyMessages()

        if (response.isSuccessful) {
            val messageResList: MutableList<MessageResDTO> = response.body()!!.toMutableList()

            if (messageResList.isNotEmpty()) {

                for (messageRes in messageResList) {

                    val databaseMessage =
                        messageLocalDataSource.getMessageByWebId(messageRes.id, false)

                    if (databaseMessage == null) {
                        val message = MessageResDTO.toMessageEntity(
                            null, messageRes, Constants.IsMine.NO.value
                        )

                        if (BuildConfig.ENCRYPT_API) {
                            message.encryptBody(cryptoMessage)
                        }

                        val messageId = messageLocalDataSource.insertMessage(message)

                        if (messageRes.quoted.isNotEmpty()) {
                            insertQuote(messageRes, messageId.toInt())
                        }

                        attachmentLocalDataSource.insertAttachments(
                            AttachmentResDTO.toListConversationAttachment(
                                messageId.toInt(),
                                messageRes.attachments
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun insertQuote(messageRes: MessageResDTO, messageId: Int) {
        val originalMessage =
            messageLocalDataSource.getMessageByWebId(messageRes.quoted, false)

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

    override suspend fun getDeletedMessages() {
        try {
            val response = napoleonApi.getDeletedMessages()
            if (response.isSuccessful && (response.body()!!.count() > 0)) {
                messageLocalDataSource.deletedMessages(response.body()!!)
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override suspend fun insertSubscription() {
        try {
            val response = napoleonApi.getSubscriptionUser()
            if (response.isSuccessful) {
                if (response.body()!!.dateExpires != 0L) {
                    sharedPreferencesManager.putInt(
                        Constants.SharedPreferences.PREF_TYPE_SUBSCRIPTION,
                        response.body()!!.subscriptionId
                    )
                    sharedPreferencesManager.putLong(
                        Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME,
                        TimeUnit.SECONDS.toMillis(response.body()!!.dateExpires)
                    )
                    if (response.body()!!.dateExpires > System.currentTimeMillis()) {
                        sharedPreferencesManager.putLong(
                            Constants.SharedPreferences.PREF_FREE_TRIAL,
                            1
                        )
                    }
                } else {
                    sharedPreferencesManager.putInt(
                        Constants.SharedPreferences.PREF_TYPE_SUBSCRIPTION,
                        0
                    )
                    sharedPreferencesManager.putLong(
                        Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME,
                        0L
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun getFreeTrial(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_FREE_TRIAL
        )
    }

    override fun getSubscriptionTime(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME
        )
    }

    override fun getJsonNotification(): String {
        return sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_JSON_NOTIFICATION, ""
        )
    }

    override fun getContact(contactId: Int): Contact? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override fun cleanJsonNotification() {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_JSON_NOTIFICATION, ""
        )
    }

    override fun getMessagesForHome(): LiveData<List<MessageAndAttachment>> {
        return messageLocalDataSource.getMessagesForHome()
    }

    override fun verifyMessagesToDelete() {
        messageLocalDataSource.verifyMessagesToDelete()
    }
}