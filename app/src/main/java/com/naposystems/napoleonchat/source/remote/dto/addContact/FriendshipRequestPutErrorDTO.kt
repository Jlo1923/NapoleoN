package com.naposystems.napoleonchat.source.remote.dto.addContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestPutErrorDTO(
    @Json(name = "error") val error: String
)