package com.naposystems.napoleonchat.source.remote.dto.addContact

import com.naposystems.napoleonchat.source.remote.dto.contacts.ContactResDTO
import com.naposystems.napoleonchat.model.FriendShipRequest
import com.naposystems.napoleonchat.utility.Constants
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestOfferDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "user_offer") val userOffer: Int,
    @Json(name = "user_obtainer") val userObtainer: Int,
    @Json(name = "state") val state: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "friendship_request_obtainer") val contact: ContactResDTO
) {
    companion object {
        fun toFriendshipRequestEntity(response: FriendshipRequestOfferDTO): FriendShipRequest {
            val friendshipRequest = FriendShipRequest(
                response.id,
                response.userOffer,
                response.userObtainer,
                response.state,
                response.createdAt,
                ContactResDTO.toEntity(response.contact),
                false
            )

            friendshipRequest.type = Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_OFFER.type

            return friendshipRequest
        }
    }
}