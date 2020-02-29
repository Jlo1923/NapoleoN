package com.naposystems.pepito.ui.home

import androidx.lifecycle.LiveData
import com.naposystems.pepito.dto.home.FriendshipRequestQuantityResDTO
import com.naposystems.pepito.entity.User
import com.naposystems.pepito.entity.conversation.ConversationAndContact
import retrofit2.Response

interface IContractHome {

    interface ViewModel {
        fun getUser(): User
        fun getFriendshipQuantity()
        fun subscribeToGeneralSocketChannel()
        fun getContactsAndMessages()
    }

    interface Repository {
        suspend fun getUser(): User
        suspend fun getFriendshipQuantity(): Response<FriendshipRequestQuantityResDTO>
        suspend fun subscribeToGeneralSocketChannel()
        fun getConversations(): LiveData<List<ConversationAndContact>>
        suspend fun getRemoteMessages()
        suspend fun getContacts()
    }
}