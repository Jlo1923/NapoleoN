package com.naposystems.pepito.dto.recoveryAccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountReqDTO(
    @Json(name = "nick") val nickName: String
)