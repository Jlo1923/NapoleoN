package com.naposystems.pepito.ui.home

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import retrofit2.Response

interface IContractHome {

    interface ViewModel {
        fun getUserLiveData()
        fun getFriendshipQuantity()
        fun subscribeToGeneralSocketChannel()
        fun getContactsAndMessages()
        fun getDeletedMessages()
        fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
        fun getJsonNotification()
        fun getContact(contactId : Int)
        fun cleanJsonNotification()
    }

    interface Repository {
        suspend fun getUserLiveData(): LiveData<User>
        suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO>
        suspend fun subscribeToGeneralSocketChannel()
        fun getConversations(): LiveData<List<ConversationAndContact>>
        suspend fun getRemoteMessages()
        suspend fun getContacts()
        suspend fun getDeletedMessages()
        suspend fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
        fun getJsonNotification() : String
        fun getContact(contactId : Int) : Contact
        fun cleanJsonNotification()
    }
}