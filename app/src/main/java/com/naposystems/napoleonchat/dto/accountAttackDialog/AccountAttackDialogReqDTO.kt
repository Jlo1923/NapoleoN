package com.naposystems.napoleonchat.dto.accountAttackDialog

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountAttackDialogReqDTO (
    @Json(name = "attacker_id") val attackerId: String
)