package com.naposystems.napoleonchat.source.remote.dto.recoveryAccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountReqDTO(
    @Json(name = "nick") val nickName: String
)