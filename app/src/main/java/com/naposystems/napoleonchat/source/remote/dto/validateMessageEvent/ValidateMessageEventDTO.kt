package com.naposystems.napoleonchat.source.remote.dto.validateMessageEvent

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//TODO: Crear metodo sobreescrito para un unico mensaje
@JsonClass(generateAdapter = true)
data class ValidateMessageEventDTO(
    @Json(name = "messages") val messages: List<ValidateMessage>
)

@JsonClass(generateAdapter = true)
data class ValidateMessage(
    @Json(name = "id") val id: String,
    @Json(name = "user") val user: Int,
    @Json(name = "status") val status: Int
)
