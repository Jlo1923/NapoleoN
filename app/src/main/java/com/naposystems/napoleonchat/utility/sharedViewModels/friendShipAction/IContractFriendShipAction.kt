package com.naposystems.napoleonchat.utility.sharedViewModels.friendShipAction

import com.naposystems.napoleonchat.dto.addContact.FriendshipRequestPutResDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageReqDTO
import com.naposystems.napoleonchat.dto.conversation.message.MessageResDTO
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest
import com.naposystems.napoleonchat.entity.message.Message
import retrofit2.Response

interface IContractFriendShipAction {
    interface ViewModel {
        fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest)
        fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest)
        fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest)
        fun clearMessageError()
    }

    interface Repository {
        suspend fun refuseFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO>
        suspend fun acceptFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO>
        suspend fun cancelFriendshipRequest(friendShipRequest: FriendShipRequest): Response<FriendshipRequestPutResDTO>
        fun getError(response: Response<FriendshipRequestPutResDTO>): String
        suspend fun addContact(friendShipRequest: FriendShipRequest)
        suspend fun sendNewContactMessage(messageReqDTO: MessageReqDTO): Response<MessageResDTO>
        fun insertMessage(message: Message): Long
    }
}