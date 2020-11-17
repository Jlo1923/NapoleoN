package com.naposystems.napoleonchat.dto.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DisplayNameReqDTO(
    @Json(name = "fullname") val displayName: String
)