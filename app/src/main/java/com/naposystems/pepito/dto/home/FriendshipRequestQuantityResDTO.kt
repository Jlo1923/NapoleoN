package com.naposystems.pepito.dto.home

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestQuantityResDTO(
    @Json(name = "countFriendshipRequestReceived") val quantityFriendshipRequestReceived: Int,
    @Json(name = "countFriendshipRequestOffer") val quantityFriendshipRequestOffer: Int
)