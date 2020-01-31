package com.naposystems.pepito.dto.addContact

import com.naposystems.pepito.dto.contacts.ContactResDTO
import com.naposystems.pepito.entity.addContact.FriendShipRequest
import com.naposystems.pepito.utility.Constants
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
        fun toFriendshipRequestEntity(response: FriendshipRequestReceivedDTO): FriendShipRequest {
            val friendshipRequest = FriendShipRequest(
                response.id,
                response.userOffer,
                response.userObtainer,
                response.state,
                response.createdAt,
                ContactResDTO.toEntity(response.contact),
                true
            )

            friendshipRequest.type = Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_RECEIVED.type

            return friendshipRequest
        }
    }
}