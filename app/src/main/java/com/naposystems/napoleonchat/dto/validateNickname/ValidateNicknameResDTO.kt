package com.naposystems.napoleonchat.dto.validateNickname

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateNicknameResDTO(
    @Json(name = "nick_exist") val nicknameExist: Boolean
)