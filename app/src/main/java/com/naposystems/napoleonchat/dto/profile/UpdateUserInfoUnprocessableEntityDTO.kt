package com.naposystems.napoleonchat.dto.profile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserInfoUnprocessableEntityDTO(
    @Json(name = "names") val displayName: List<String> = emptyList(),
    @Json(name = "language_iso") val languageIso: List<String> = emptyList(),
    @Json(name = "firebase_id") val firebaseId: List<String> = emptyList(),
    @Json(name = "state") val state: List<String> = emptyList()
)