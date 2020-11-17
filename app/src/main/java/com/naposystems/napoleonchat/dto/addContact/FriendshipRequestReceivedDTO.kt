package com.naposystems.napoleonchat.dto.addContact

import com.naposystems.napoleonchat.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequest
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestReceivedDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "user_offer") val userOffer: Int,
    @Json(name = "user_obtainer") val userObtainer: Int,
    @Json(name = "state") val state: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "friendship_request_offer") val contact: ContactResDTO
) {
    companion object {
        fun toListFriendshipRequestReceivedEntity(
            listFriendshipRequestReceivedDTO: List<FriendshipRequestReceivedDTO>
        ): List<FriendShipRequest> {

            val listFriendShip: MutableList<FriendShipRequest> = arrayListOf()

            for (friendShipReceivedDTO in listFriendshipRequestReceivedDTO) {
                listFriendShip.add(
                    FriendShipRequest(
                        id = friendShipReceivedDTO.id,
                        userOffer = friendShipReceivedDTO.userOffer,
                        userObtainer = friendShipReceivedDTO.userObtainer,
                        state = friendShipReceivedDTO.state,
                        createdAt = friendShipReceivedDTO.createdAt,
                        contact = ContactResDTO.toEntity(friendShipReceivedDTO.contact),
                        isReceived = true
                    )
                )
            }

            return listFriendShip
        }

        fun toFriendshipRequestEntity(response: FriendshipRequestReceivedDTO): FriendShipRequest {
            val friendshipRequest = FriendShipRequest(
                id = response.id,
                userOffer = response.userOffer,
                userObtainer = response.userObtainer,
                state = response.state,
                createdAt = response.createdAt,
                contact = ContactResDTO.toEntity(response.contact),
                isReceived = true
            )

            friendshipRequest.type =
                Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_RECEIVED.type

            return friendshipRequest
        }
    }
}