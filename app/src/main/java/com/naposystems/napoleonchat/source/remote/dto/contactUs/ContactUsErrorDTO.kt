package com.naposystems.napoleonchat.source.remote.dto.contactUs

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactUsErrorDTO(
    @Json(name = "error") val error: String
)