package com.naposystems.napoleonchat.dto.addContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestPutResDTO(
    @Json(name = "success") val success: Boolean
)