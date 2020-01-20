package com.naposystems.pepito.dto.profile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserInfoReqDTO(
    @Json(name = "fullname") val displayName: String = "",
    @Json(name = "avatar") val avatar: String = "",
    @Json(name = "language_iso") val languageIso: String = "",
    @Json(name = "firebase_id") val firebaseId: String = "",
    @Json(name = "my_status") val status: String = "",
    @Json(name = "state") val state: String = ""
)