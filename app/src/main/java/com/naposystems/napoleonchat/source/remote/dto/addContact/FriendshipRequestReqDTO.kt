package com.naposystems.napoleonchat.source.remote.dto.addContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestReqDTO(
    @Json(name = "user_obtainer") val contactId: Int
)