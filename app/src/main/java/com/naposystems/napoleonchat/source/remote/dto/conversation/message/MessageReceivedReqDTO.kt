package com.naposystems.napoleonchat.source.remote.dto.conversation.message

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

const val MESSAGE = 0
const val ATTACHMENT = 1

const val UNREAD_OR_RECEIVED = 1
const val READ = 2

@JsonClass(generateAdapter = true)
data class MessageReceivedReqDTO(
    @Json(name = "message_id")
    val messageId: String,
    @Json(name = "user")
    val userSender: Long? = null,
    @Json(name = "type")
    val type: Int,
    @Json(name = "status")
    val status: Int? = null
)