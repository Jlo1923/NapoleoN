package com.naposystems.pepito.ui.home

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.message.MessageAndAttachment
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
        fun cleanJsonNotification()
        fun resetConversations()
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
        fun cleanJsonNotification()
        fun getMessagesForHome(): LiveData<List<MessageAndAttachment>>
        fun verifyMessagesToDelete()
    }
}