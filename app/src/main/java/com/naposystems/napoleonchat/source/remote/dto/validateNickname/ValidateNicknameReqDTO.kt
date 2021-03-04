package com.naposystems.napoleonchat.source.remote.dto.validateNickname

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateNicknameReqDTO(
    @Json(name = "nick") val nickname: String
)