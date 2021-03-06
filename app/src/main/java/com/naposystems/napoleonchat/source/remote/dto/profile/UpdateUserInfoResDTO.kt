package com.naposystems.napoleonchat.source.remote.dto.profile

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateUserInfoResDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "fullname") val displayName: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "my_status") val status: String,
    @Json(name = "lastseen") val lastSeen: String,
    @Json(name = "avatar") val avatarUrl: String,
    @Json(name = "language_iso") val languageIso: String,
    @Json(name = "type") val type: Int,
    @Json(name = "membership") val membership: Boolean,
    @Json(name = "data_update") val dataUpdate: String?
)