package com.naposystems.napoleonchat.source.remote.dto.contacts.unblockContact

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnblockContactResDTO (
    @Json(name = "success") val success: Boolean
)