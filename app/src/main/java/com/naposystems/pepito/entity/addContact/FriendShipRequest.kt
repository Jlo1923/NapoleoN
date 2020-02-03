package com.naposystems.pepito.entity.addContact

import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.utility.Constants

class FriendShipRequest(
    val id: Int,
    val userOffer: Int,
    val userObtainer: Int,
    val state: String,
    val createdAt: String,
    val contact: Contact,
    val isReceived: Boolean = false
) : FriendShipRequestAdapterType(id, Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_RECEIVED.type)