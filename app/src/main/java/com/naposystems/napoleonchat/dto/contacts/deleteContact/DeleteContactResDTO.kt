package com.naposystems.napoleonchat.dto.contacts.deleteContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteContactResDTO (
    @Json(name = "success") val success: Boolean
)