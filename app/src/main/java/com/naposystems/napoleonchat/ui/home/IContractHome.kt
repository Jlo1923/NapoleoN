package com.naposystems.napoleonchat.ui.home

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.entity.User
import com.naposystems.napoleonchat.entity.message.MessageAndAttachment
import retrofit2.Response

interface IContractHome {

    interface ViewModel {
        fun getConversation()
        fun getUserLiveData()
        fun getFriendshipQuantity()
        fun subscribeToGeneralSocketChannel()
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
    }

    interface Repository {
        suspend fun getUserLiveData(): LiveData<User>
        suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO>
        suspend fun subscribeToGeneralSocketChannel()
        suspend fun getRemoteMessages()
        suspend fun getDeletedMessages()
        suspend fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
        fun getJsonNotification() : String
        fun getContact(contactId : Int) : Contact?
        suspend fun cleanJsonNotification()
        fun getMessagesForHome(): LiveData<List<MessageAndAttachment>>
        fun verifyMessagesToDelete()
    }
}