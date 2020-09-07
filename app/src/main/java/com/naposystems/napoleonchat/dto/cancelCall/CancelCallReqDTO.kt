package com.naposystems.napoleonchat.dto.cancelCall

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CancelCallReqDTO(
    @Json(name = "contact_id") val contactId: Int
)