package com.naposystems.napoleonchat.source.remote.dto.accessPin

import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountResDTO(
    @Json(name = "fullname") val fullName: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "id") val id: Int,
    @Json(name = "my_status") val status: String = "",
    @Json(name = "secret_key") val secretKey: String,
    @Json(name = "type") val userType: Int,
    @Json(name = "created_at") val createAt: Long
) {
    companion object {
        fun toUserModel(
            createAccountResDTO: CreateAccountResDTO,
            firebaseId: String,
            accessPin: String,
            status: String
        ): UserEntity {
            return UserEntity(
                firebaseId = firebaseId,
                id = createAccountResDTO.id,
                nickname = createAccountResDTO.nickname,
                displayName =  createAccountResDTO.fullName,
                accessPin = accessPin,
                imageUrl = "",
                status = status,
                headerUri = "",
                chatBackground = "",
                type = createAccountResDTO.userType,
                createAt = createAccountResDTO.createAt
            )
        }
    }
}