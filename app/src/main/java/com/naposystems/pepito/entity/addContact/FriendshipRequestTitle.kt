package com.naposystems.pepito.entity.addContact

import com.naposystems.pepito.utility.Constants

data class FriendshipRequestTitle(
    val id: Int,
    val title: String
) : FriendShipRequestAdapterType(id, Constants.FriendShipRequestType.TITLE.type)