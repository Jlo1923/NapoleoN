package com.naposystems.napoleonchat.repository.home

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestReceivedDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.remote.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.QuoteEntity
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.home.IContractHome
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketMessageService: SocketMessageService,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource
) :
    IContractHome.Repository {

    private val firebaseId by lazy {
        sharedPreferencesManager.getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
    }

    override suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO> {
        return napoleonApi.getFriendshipRequestQuantity()
    }

    override suspend fun getFriendshipRequestHome(): Response<List<FriendshipRequestReceivedDTO>> {
        return napoleonApi.getFriendShipRequestReceivedHome()
    }

    override suspend fun getUserLiveData(): LiveData<UserEntity> {
        val firebaseId = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_FIREBASE_ID, ""
        )
        return userLocalDataSourceImp.getUserLiveData(firebaseId)
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

//                        if (BuildConfig.ENCRYPT_API) {
//                            message.encryptBody(cryptoMessage)
//                        }

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
            var firstAttachmentEntity: AttachmentEntity? = null

            if (originalMessage.attachmentEntityList.isNotEmpty()) {
                firstAttachmentEntity = originalMessage.attachmentEntityList.first()
            }

            val quote = QuoteEntity(
                id = 0,
                messageId = messageId,
                contactId = originalMessage.messageEntity.contactId,
                body = originalMessage.messageEntity.body,
                attachmentType = firstAttachmentEntity?.type ?: "",
                thumbnailUri = firstAttachmentEntity?.fileName ?: "",
                messageParentId = originalMessage.messageEntity.id,
                isMine = originalMessage.messageEntity.isMine
            )

            quoteLocalDataSource.insertQuote(quote)
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

    override fun getContact(contactId: Int): ContactEntity? {
        return contactLocalDataSource.getContactById(contactId)
    }

    override suspend fun cleanJsonNotification() {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_JSON_NOTIFICATION, ""
        )
    }

    override fun getMessagesForHome(): LiveData<List<MessageAttachmentRelation>> {
        return messageLocalDataSource.getMessagesForHome()
    }

    override fun verifyMessagesToDelete() {
        messageLocalDataSource.verifyMessagesToDelete()
    }

    override fun getDialogSubscription(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_DIALOG_SUBSCRIPTION)
    }

    override fun setDialogSubscription() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_DIALOG_SUBSCRIPTION,
            Constants.ShowDialogSubscription.NO.option
        )
    }

    override suspend fun deleteDuplicatesMessages() {
        messageLocalDataSource.deleteDuplicatesMessages()
    }

    override suspend fun addUUID() {
        messageLocalDataSource.addUUID()
    }
}