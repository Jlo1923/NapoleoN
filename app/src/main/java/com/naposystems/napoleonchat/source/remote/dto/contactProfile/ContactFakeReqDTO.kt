package com.naposystems.napoleonchat.source.remote.dto.contactProfile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactFakeReqDTO(
    @Json(name = "full_name_fake") val nameFake: String?,
    @Json(name = "nick_fake") val nicknameFake: String?,
    @Json(name = "avatar_fake") val avatarFake: String?
)
