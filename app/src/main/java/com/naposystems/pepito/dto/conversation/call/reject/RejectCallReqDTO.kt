package com.naposystems.pepito.dto.conversation.call.reject

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RejectCallReqDTO(
    @Json(name = "contact_id") val contactId: Int,
    @Json(name = "channel") val channel: String
)