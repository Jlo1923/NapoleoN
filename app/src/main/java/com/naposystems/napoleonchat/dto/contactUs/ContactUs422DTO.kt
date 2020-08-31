package com.naposystems.napoleonchat.dto.contactUs

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactUs422DTO (
    @Json(name = "category_pqrs_id") val categoryPqrsId: List<String> = ArrayList(),
    @Json(name = "description") val description: List<String> = ArrayList(),
    @Json(name = "maker") val maker: List<String> = ArrayList(),
    @Json(name = "model") val model: List<String> = ArrayList(),
    @Json(name = "system_version") val systemVersion: List<String> = ArrayList(),
    @Json(name = "application_version") val applicationVersion: List<String> = ArrayList()
    )