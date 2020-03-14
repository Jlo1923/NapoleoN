package com.naposystems.pepito.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import retrofit2.Response

interface IContractHome {

    interface ViewModel {
        fun getUser(): User
        fun getFriendshipQuantity()
        fun subscribeToGeneralSocketChannel()
        fun getContactsAndMessages()
        fun getDeletedMessages()
        fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
    }

    interface Repository {
        suspend fun getUser(): User
        suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO>
        suspend fun subscribeToGeneralSocketChannel()
        fun getConversations(): LiveData<List<ConversationAndContact>>
        suspend fun getRemoteMessages()
        suspend fun getContacts()
        suspend fun getDeletedMessages()
        suspend fun insertSubscription()
        fun getFreeTrial(): Long
        fun getSubscriptionTime(): Long
    }
}