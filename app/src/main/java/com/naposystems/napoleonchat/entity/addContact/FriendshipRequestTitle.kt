package com.naposystems.napoleonchat.entity.addContact

import com.naposystems.napoleonchat.utility.Constants

data class FriendshipRequestTitle(
    val id: Int,
    val title: String = ""
) : FriendShipRequestAdapterType(id, Constants.FriendShipRequestType.TITLE.type)