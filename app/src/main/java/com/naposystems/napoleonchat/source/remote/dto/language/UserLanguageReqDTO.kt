package com.naposystems.napoleonchat.source.remote.dto.language

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLanguageReqDTO(
    @Json(name = "language_iso") val languageIso: String
)