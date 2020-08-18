package com.naposystems.napoleonchat.dto.contacts.blockedContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BlockedContactResDTO(
    @Json(name = "success") val success: Boolean
)