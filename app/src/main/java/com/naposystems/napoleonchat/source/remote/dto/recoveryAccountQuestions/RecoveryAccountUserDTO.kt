package com.naposystems.napoleonchat.source.remote.dto.recoveryAccountQuestions

import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountUserDTO(
    @Json(name = "id") val id: Int,
    @Json(name = "fullname") val fullname: String,
    @Json(name = "nick") val nick: String,
    @Json(name = "my_status") val myStatus: String,
    @Json(name = "lastseen") val lastseen: String,
    @Json(name = "avatar") val avatar: String,
    @Json(name = "language_iso") val languageIso: String,
    @Json(name = "secret_key") val secretKey: String,
    @Json(name = "type") val userType: Int,
    @Json(name = "created_at") val createAt: Long
) {

    companion object {
        fun toUserModel(recoveryAccountUserDTO: RecoveryAccountUserDTO, firebaseId: String): UserEntity {
            return UserEntity(
                firebaseId = firebaseId,
                id = recoveryAccountUserDTO.id,
                nickname = recoveryAccountUserDTO.nick,
                displayName = recoveryAccountUserDTO.fullname,
                accessPin = "",
                imageUrl = recoveryAccountUserDTO.avatar,
                status = recoveryAccountUserDTO.myStatus,
                headerUri = "",
                chatBackground = "",
                type = recoveryAccountUserDTO.userType,
                createAt = recoveryAccountUserDTO.createAt
            )
        }
    }
}