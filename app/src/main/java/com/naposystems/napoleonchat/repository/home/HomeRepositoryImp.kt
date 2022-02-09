package com.naposystems.napoleonchat.repository.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.naposystems.napoleonchat.model.SubscriptionStatus
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.subscription.SubscriptionWorker
import com.naposystems.napoleonchat.service.syncManager.SyncManager
import com.naposystems.napoleonchat.source.local.datasource.attachment.AttachmentLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.message.MessageLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.quoteMessage.QuoteLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.*
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestReceivedDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.attachment.AttachmentResDTO
import com.naposystems.napoleonchat.source.remote.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.source.remote.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessageDTO
import com.naposystems.napoleonchat.source.remote.dto.messagesReceived.MessagesReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeRepositoryImp @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val messageLocalDataSource: MessageLocalDataSource,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val attachmentLocalDataSource: AttachmentLocalDataSource,
    private val quoteLocalDataSource: QuoteLocalDataSource,
    private val syncManager: SyncManager,
    private val context: Context,
    private val workManager: WorkManager
) :
    HomeRepository {

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

                        val messageId = messageLocalDataSource.insertMessage(message)

                        if (messageRes.quoted.isNotEmpty()) {
                            insertQuote(messageRes, messageId.toInt())
                        }
                        handlerAttachments(
                            messageRes.attachments,
                            messageId.toInt(),
                            message.contactId
                        )
                    }
                }
            }
        }
    }

    @Synchronized
    suspend fun handlerAttachments(
        attachments: List<AttachmentResDTO>,
        messageId: Int,
        contactId: Int
    ) {
        val listAttachments = AttachmentResDTO.toListConversationAttachment(
            messageId,
            attachments
        )

        /**
         * Solo hacemos insersion de attachments sino existe
         * por medio de su id
         */
        listAttachments.forEach { attachment ->
            attachmentLocalDataSource.apply {
                if (this.existAttachmentByWebId(attachment.webId).not()) {
                    this.insertAttachments(listOf(attachment))
                }

                val listUniqueAttachment = MessageDTO(
                    id = attachment.webId,
                    type = Constants.MessageType.ATTACHMENT.type,
                    user = contactId,
                    status = Constants.StatusMustBe.RECEIVED.status
                )
                notifyMessageReceivedRemote(MessagesReqDTO(listOf(listUniqueAttachment)))
            }
        }
    }

    fun notifyMessageReceivedRemote(messagesReqDTO: MessagesReqDTO) {

        Timber.d("**Paso 9: Proceso consumir recibido del item $messagesReqDTO")

        GlobalScope.launch {
            try {
                Timber.d("**Paso 9.1: Proceso consumir recibido del item $messagesReqDTO")
                napoleonApi.notifyMessageReceived(messagesReqDTO)
            } catch (e: Exception) {
                Timber.e(e)
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
            if (response.isSuccessful) {
                response.body()?.messagesId.let {
                    it?.let {
                        messageLocalDataSource.deleteMessagesByWebId(it)
                    }
                }

                response.body()?.attachmentsId.let {
                    it?.let {
                        attachmentLocalDataSource.deletedAttachments(it)
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    //override suspend fun getDeletedMessages() = syncManager.getDeletedMessages()

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

    override suspend fun lastSubscription() {
        val response = napoleonApi.lastSubscription()
        if (response.isSuccessful) {
            val expire = response.body()!!.expire
            val status = response.body()!!.status
            val createdAt = userLocalDataSourceImp.getMyUser().createAt
            val currentTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            val currentDaySinceCreated =
                TimeUnit.SECONDS.toDays(currentTimeInSeconds - createdAt)
            val currentDaySinceExpired = TimeUnit.SECONDS.toDays(expire - currentTimeInSeconds)
            val type = getSubscriptionType(status, currentDaySinceExpired, currentDaySinceCreated)
            saveSubscription(
                type,
                userLocalDataSourceImp.getMyUser().id.toString(), createdAt
            )
        }
    }

    private fun getSubscriptionType(
        isActive: Boolean,
        subscriptionExpireDays: Long,
        currentDaySinceCreated: Long
    ) = when {
        isActive -> SubscriptionStatus.ACTIVE
        currentDaySinceCreated < 4 -> SubscriptionStatus.FREE_TRIAL
        currentDaySinceCreated in 4..7 -> SubscriptionStatus.FREE_TRIAL_DAY_4
        currentDaySinceCreated >= 38 -> SubscriptionStatus.TOTAL_LOCK
        currentDaySinceCreated in 8..36 -> SubscriptionStatus.PARTIAL_LOCK
        subscriptionExpireDays in -30..0 -> SubscriptionStatus.PARTIAL_LOCK
        subscriptionExpireDays <= 0 -> SubscriptionStatus.TOTAL_LOCK
        else -> SubscriptionStatus.FREE_TRIAL
    }

    private fun saveSubscription(
        subscriptionStatus: SubscriptionStatus,
        userId: String,
        userCreatedAt: Long
    ) {
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.SubscriptionStatus,
            subscriptionStatus.name
        )
        sharedPreferencesManager.putString(
            Constants.SharedPreferences.PREF_USER_ID,
            userId
        )
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_USER_CREATED_AT,
            userCreatedAt
        )
        RxBus.publish(RxEvent.SubscriptionStatusEvent(subscriptionStatus))
        subscriptionNotificationWork()
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

    override fun getMessagesForHome(): LiveData<MutableList<MessageAttachmentRelation>> {
        return messageLocalDataSource.getMessagesForHome()
    }

    override suspend fun verifyMessagesToDelete() {

        messageLocalDataSource.deleteMessagesByTotalSelfDestructionAt()

        /**
         * Debemos eliminar los attachments cuyo tiempo de autodestruction ya venció, si se elimina
         * debemos eliminar su archivo en cache
         * si todos los attachments son eliminados, debemos eliminar el mensaje
         */
        val attachments = attachmentLocalDataSource.getAttachmentsSelfDestructionExpired()

        // Tomamos los ids de los mensajes padres
        val hashMap: HashMap<String, String> = HashMap()
        attachments.forEach {
            if (hashMap.containsKey(it.messageWebId).not()) {
                hashMap[it.messageWebId] = it.messageWebId
            }
        }

        // eliminamos los attachments
        attachments.forEach { it.deleteFile(context) }
        attachmentLocalDataSource.deletedAttachments(attachments.map { it.webId })

        /*
        vamos a consultar los mensajes por medio de los webid que obtuvimos, si su cantidad de
        attachments es 0, debemos eliminarlo
         */
        for ((key, value) in hashMap) {
            val message = messageLocalDataSource.getMessageByWebId(key, false)
            message?.let {
                if (it.attachmentEntityList.isEmpty()) {
                    messageLocalDataSource.deleteMessagesByWebId(listOf(key))
                }
            }
        }
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

    override suspend fun deleteDuplicates() {
        messageLocalDataSource.deleteDuplicateMessage()
        messageLocalDataSource.deleteDuplicateAttachment()
    }

    override suspend fun addUUID() {
        messageLocalDataSource.addUUIDMessage()
        messageLocalDataSource.addUUIDAttachment()
    }

    override fun verifyMessagesReceived() {
        syncManager.verifyMessagesReceived()
    }

    override fun verifyMessagesRead() {
        syncManager.verifyMessagesRead()
    }

    override fun subscriptionNotificationWork() {
        val constraintsBuilder: Constraints.Builder = Constraints.Builder()
        constraintsBuilder.setRequiresBatteryNotLow(false)
        constraintsBuilder.setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        constraintsBuilder.setRequiresCharging(false)
        val request = PeriodicWorkRequest.Builder(
            SubscriptionWorker::class.java,
            12,
            TimeUnit.HOURS
        )
        request.setConstraints(constraintsBuilder.build())
        workManager.enqueueUniquePeriodicWork(
            "SUBSCRIPTION_NOTIFICATION_WORK",
            ExistingPeriodicWorkPolicy.REPLACE,
            request.build()
        )
    }
}