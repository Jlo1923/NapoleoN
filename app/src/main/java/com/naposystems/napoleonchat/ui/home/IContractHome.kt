package com.naposystems.napoleonchat.ui.home

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.remote.dto.addContact.FriendshipRequestReceivedDTO
import com.naposystems.napoleonchat.source.remote.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.napoleonchat.source.local.entity.ContactEntity
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.source.local.entity.MessageAttachmentRelation
import retrofit2.Response

interface IContractHome {

    interface ViewModel {
        fun getConversation()
        fun getUserLiveData()
        fun getFriendshipQuantity()
        fun getFriendshipRequestHome()
        fun getMessages()
        fun getDeletedMessages()
        fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
        fun getJsonNotification()
        fun getContact(contactId : Int)
        fun cleanJsonNotification(json : String)
        fun resetConversations()
        fun cleanVariables()
        fun verifyMessagesToDelete()
        fun getDialogSubscription(): Int
        fun setDialogSubscription()
    }

    interface Repository {
        suspend fun getUserLiveData(): LiveData<UserEntity>
        suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO>
        suspend fun getFriendshipRequestHome(): Response<List<FriendshipRequestReceivedDTO>>
        suspend fun getRemoteMessages()
        suspend fun getDeletedMessages()
        suspend fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
        fun getJsonNotification() : String
        fun getContact(contactId : Int) : ContactEntity?
        suspend fun cleanJsonNotification()
        fun getMessagesForHome(): LiveData<List<MessageAttachmentRelation>>
        fun verifyMessagesToDelete()
        fun getDialogSubscription(): Int
        fun setDialogSubscription()
    }
}