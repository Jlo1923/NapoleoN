package com.naposystems.napoleonchat.source.remote.dto.status

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserStatusReqDTO(
    @Json(name = "my_status") val status: String
)