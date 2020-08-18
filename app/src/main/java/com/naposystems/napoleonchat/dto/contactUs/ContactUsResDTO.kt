package com.naposystems.napoleonchat.dto.contactUs

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactUsResDTO (
    @Json(name = "category_pqrs_id") val categoryId: Int,
    @Json(name = "description") val description: String,
    @Json(name = "maker") val maker: String,
    @Json(name = "model") val model: String,
    @Json(name = "system_version") val systemVersion: String,
    @Json(name = "application_version") val applicationVersion: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "date_create") val dateCreate: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "id") val id: String
    )