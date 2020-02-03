package com.naposystems.pepito.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.ArrayList

@JsonClass(generateAdapter = true)
data class MessageReqDTO(
    @Json(name = "user_receiver") val userDestination: Int,
    @Json(name = "quoted") val quoted: String,
    @Json(name = "body") val body: String,
    @Json(name = "attachments") val attachments: List<AttachmentReqDTO> = ArrayList()
)