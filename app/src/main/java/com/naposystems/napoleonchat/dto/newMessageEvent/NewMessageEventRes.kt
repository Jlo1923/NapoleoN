package com.naposystems.napoleonchat.dto.newMessageEvent

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewMessageEventRes(
    @Json(name = "data") val data: NewMessageDataEventRes
)

@JsonClass(generateAdapter = true)
data class NewMessageDataEventRes(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "contact_id") val contactId: Int
)