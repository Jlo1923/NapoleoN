package com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountAttackDialogResDTO (
    @Json(name = "success") val success: Boolean
)