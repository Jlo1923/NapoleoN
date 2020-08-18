package com.naposystems.napoleonchat.dto.addContact

import android.content.Context
import com.naposystems.napoleonchat.entity.addContact.FriendShipRequestAdapterType
import com.naposystems.napoleonchat.entity.addContact.FriendshipRequestTitle
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestsResDTO(
    @Json(name = "friendshipRequestReceived") val friendshipRequestReceived: List<FriendshipRequestReceivedDTO>,
    @Json(name = "friendshipRequestOffer") val friendshipRequestOffer: List<FriendshipRequestOfferDTO>
) {
    companion object {
        fun toListFriendshipRequestEntity(
            response: FriendshipRequestsResDTO,
            context: Context
        ): List<FriendShipRequestAdapterType> {
            val list: MutableList<FriendShipRequestAdapterType> = ArrayList()

            if (response.friendshipRequestReceived.isNotEmpty()) {
                val receivedTitle = FriendshipRequestTitle(-1)

                list.add(receivedTitle)

                for (friendshipRequestReceived in response.friendshipRequestReceived) {
                    list.add(
                        FriendshipRequestReceivedDTO
                            .toFriendshipRequestEntity(friendshipRequestReceived)
                    )
                }
            }

            if (response.friendshipRequestOffer.isNotEmpty()) {
                val offerTitle = FriendshipRequestTitle(-2)

                list.add(offerTitle)

                for (friendshipRequestOffer in response.friendshipRequestOffer) {
                    list.add(
                        FriendshipRequestOfferDTO
                            .toFriendshipRequestEntity(friendshipRequestOffer)
                    )
                }
            }

            return list
        }
    }
}