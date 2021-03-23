package com.naposystems.napoleonchat.source.remote.dto.contactProfile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactFakeResDTO(
    @Json(name = "id") val id: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "fullname") val fullname: String?,
    @Json(name = "my_status") val status: String?,
    @Json(name = "lastseen") val lastseen: Double?,
    @Json(name = "avatar") val avatar: String?,
    @Json(name = "nick_fake") val nicknameFake: String?,
    @Json(name = "full_name_fake") val fullNameFake: String?,
    @Json(name = "avatar_fake") val avatarFake: String?,
)
