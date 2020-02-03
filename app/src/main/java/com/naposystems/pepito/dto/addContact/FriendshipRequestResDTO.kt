package com.naposystems.pepito.dto.addContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FriendshipRequestResDTO(
    @Json(name = "success") val success: Boolean
)