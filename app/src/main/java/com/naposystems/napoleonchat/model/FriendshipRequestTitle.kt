package com.naposystems.napoleonchat.model

import com.naposystems.napoleonchat.utility.Constants

data class FriendshipRequestTitle(
    val id: Int,
    val title: String = ""
) : FriendShipRequestAdapterType(id, Constants.FriendShipRequestType.TITLE.type)