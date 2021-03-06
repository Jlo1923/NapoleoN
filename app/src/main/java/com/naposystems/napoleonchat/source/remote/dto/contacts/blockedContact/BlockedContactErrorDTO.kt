package com.naposystems.napoleonchat.source.remote.dto.contacts.blockedContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BlockedContactErrorDTO(
    @Json(name = "error") val error: String
)