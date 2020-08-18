package com.naposystems.napoleonchat.entity.addContact

import com.naposystems.napoleonchat.entity.Contact
import com.naposystems.napoleonchat.utility.Constants

class FriendShipRequest(
    val id: Int,
    val userOffer: Int,
    val userObtainer: Int,
    val state: String,
    val createdAt: String,
    val contact: Contact,
    val isReceived: Boolean = false
) : FriendShipRequestAdapterType(id, Constants.FriendShipRequestType.FRIENDSHIP_REQUEST_RECEIVED.type)