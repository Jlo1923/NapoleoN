package com.naposystems.pepito.model.languageSelection

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Language(
    val id: Int,
    val language: String,
    val iso: String
)