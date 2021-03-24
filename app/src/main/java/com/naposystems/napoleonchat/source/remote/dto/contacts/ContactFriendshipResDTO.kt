package com.naposystems.napoleonchat.source.remote.dto.contacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactFriendshipResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "user_obtainer") val userObtainer: Int,
    @Json(name = "user_offer") val userOffer: Int,
    @Json(name = "state") val state: String,
)