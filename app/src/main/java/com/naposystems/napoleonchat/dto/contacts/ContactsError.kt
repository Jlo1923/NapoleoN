package com.naposystems.napoleonchat.dto.contacts

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactsError(
    @Json(name = "error") val error: String
)