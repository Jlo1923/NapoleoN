package com.naposystems.napoleonchat.dto.accessPin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountUnprocessableEntityDTO(
    @Json(name = "fullname") val names: List<String> = ArrayList(),
    @Json(name = "password") val password: List<String> = ArrayList(),
    @Json(name = "nick") val nickname: List<String> = ArrayList(),
    @Json(name = "firebase_id") val firebaseId: List<String> = ArrayList()
)