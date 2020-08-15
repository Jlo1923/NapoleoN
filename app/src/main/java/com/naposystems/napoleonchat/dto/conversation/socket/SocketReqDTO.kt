package com.naposystems.napoleonchat.dto.conversation.socket

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.json.JSONObject

@JsonClass(generateAdapter = true)
class SocketReqDTO(
    @Json(name = "channel") val channel: String,
    @Json(name = "auth") val auth: AuthReqDTO
) {
    companion object {
        fun toJSONObject(socketReqDTO: SocketReqDTO): JSONObject {
            val jsonObject = JSONObject()
            val jsonObject2 = JSONObject()
            val jsonObject3 = JSONObject()
            jsonObject.put("channel", socketReqDTO.channel)

            jsonObject3.put("X-API-Key", socketReqDTO.auth.headers.key)

            jsonObject2.put("headers", jsonObject3)

            jsonObject.put("auth", jsonObject2)

            return jsonObject
        }
    }
}