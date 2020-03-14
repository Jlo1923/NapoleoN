package com.naposystems.pepito.dto.recoveryOlderAccountQuestions

import com.naposystems.pepito.dto.recoveryAccountQuestions.RecoveryAccountUserDTO
import com.naposystems.pepito.entity.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryOlderAccountDTO (
    @Json(name = "id") val id: Int,
    @Json(name = "nick") val nick: String,
    @Json(name = "fullname") val fullname: String,
    @Json(name = "my_status") val myStatus: String,
    @Json(name = "lastseen") val lastseen: String,
    @Json(name = "avatar") val avatar: String,
    @Json(name = "language_iso") val languageIso: String,
    @Json(name = "secret_key") val secretKey: String,
    @Json(name = "type") val userType: Int,
    @Json(name = "created_at") val createAt: Long
) {
    companion object {
        fun toUserModel(recoveryOlderAccountDTO: RecoveryOlderAccountDTO, firebaseId: String): User {
            return User(
                firebaseId = firebaseId,
                id = recoveryOlderAccountDTO.id,
                nickname = recoveryOlderAccountDTO.nick,
                displayName = recoveryOlderAccountDTO.fullname,
                accessPin = "",
                imageUrl = recoveryOlderAccountDTO.avatar,
                status = recoveryOlderAccountDTO.myStatus,
                headerUri = "",
                chatBackground = "",
                type = recoveryOlderAccountDTO.userType,
                createAt = recoveryOlderAccountDTO.createAt
            )
        }
    }
}