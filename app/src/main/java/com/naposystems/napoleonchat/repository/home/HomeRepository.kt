package com.naposystems.napoleonchat.repository.home

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestReceivedDTO
import com.naposystems.napoleonchat.source.remote.dto.home.FriendshipRequestQuantityResDTO
import retrofit2.Response

interface HomeRepository {

    suspend fun getUserLiveData(): LiveData<UserEntity>

    suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO>

    suspend fun getFriendshipRequestHome(): Response<List<FriendshipRequestReceivedDTO>>

    suspend fun getRemoteMessages()

    suspend fun getDeletedMessages()

    suspend fun insertSubscription()

    fun getFreeTrial(): Long

    fun getSubscriptionTime(): Long

    fun getJsonNotification(): String

    fun getContact(contactId: Int): ContactEntity?

    suspend fun cleanJsonNotification()

    fun getMessagesForHome(): LiveData<List<MessageAttachmentRelation>>

    /**
     * Verifica los mensajes a elinminar por su autodestruccion "vencida"
     * Tambien valida los attachments
     */
    suspend fun verifyMessagesToDelete()

    fun getDialogSubscription(): Int

    fun setDialogSubscription()

    suspend fun deleteDuplicatesMessages()

    suspend fun addUUID()

    fun verifyMessagesReceived()
    
    fun verifyMessagesRead()

}