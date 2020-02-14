package com.naposystems.pepito.dto.recoveryAccountQuestions

import com.naposystems.pepito.entity.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountUserDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "fullname") val fullname: String,
    @Json(name = "nick") val nick: String,
    @Json(name = "state") val state: String,
    @Json(name = "my_status") val myStatus: String,
    @Json(name = "lastseen") val lastseen: String,
    @Json(name = "avatar") val avatar: String,
    @Json(name = "language_iso") val languageIso: String,
    @Json(name = "secret_key") val secretKey: String
) {

    companion object {
        fun toUserModel(recoveryAccountUserDTO: RecoveryAccountUserDTO, firebaseId: String): User {
            return User(
                firebaseId,
                recoveryAccountUserDTO.id,
                recoveryAccountUserDTO.nick,
                recoveryAccountUserDTO.fullname,
                "",
                recoveryAccountUserDTO.avatar,
                recoveryAccountUserDTO.myStatus,
                "",
                ""
            )
        }
    }
}