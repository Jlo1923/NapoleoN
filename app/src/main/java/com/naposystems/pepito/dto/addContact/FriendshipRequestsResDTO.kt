package com.naposystems.pepito.dto.addContact

import android.content.Context
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.addContact.FriendShipRequestAdapterType
import com.naposystems.pepito.entity.addContact.FriendshipRequestTitle
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

            val receivedTitle = FriendshipRequestTitle(
                -1,
                context.getString(R.string.text_friend_requests_received)
            )
            val offerTitle = FriendshipRequestTitle(
                -2,
                context.getString(R.string.text_friend_requests_sent)
            )

            if (response.friendshipRequestReceived.isNotEmpty()) {
                list.add(receivedTitle)

                for (friendshipRequestReceived in response.friendshipRequestReceived) {
                    list.add(
                        FriendshipRequestReceivedDTO
                            .toFriendshipRequestEntity(friendshipRequestReceived)
                    )
                }
            }

            if (response.friendshipRequestOffer.isNotEmpty()) {
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