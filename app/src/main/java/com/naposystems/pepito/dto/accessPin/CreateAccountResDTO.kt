package com.naposystems.pepito.dto.accessPin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountResDTO(
    @Json(name = "names") val name: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "language_iso") val languageIso: String = "",
    @Json(name = "id") val id: Int,
    @Json(name = "my_status") val status: String = ""
)