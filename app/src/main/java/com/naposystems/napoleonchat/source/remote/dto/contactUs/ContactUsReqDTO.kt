package com.naposystems.napoleonchat.source.remote.dto.contactUs

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactUsReqDTO (
    @Json(name = "category_pqrs_id") val categoryId: Int,
    @Json(name = "description") val description: String,
    @Json(name = "maker") val maker: String,
    @Json(name = "model") val model: String,
    @Json(name = "system_version") val systemVersion: String,
    @Json(name = "application_version") val applicationVersion: String
)
