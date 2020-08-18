package com.naposystems.napoleonchat.dto.contacts.deleteContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class DeleteContactErrorDTO(
    @Json(name = "error") val error: String
)