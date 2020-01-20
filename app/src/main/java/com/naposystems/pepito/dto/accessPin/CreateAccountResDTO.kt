package com.naposystems.pepito.dto.accessPin

import com.naposystems.pepito.entity.User
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateAccountResDTO(
    @Json(name = "fullname") val fullName: String,
    @Json(name = "nick") val nickname: String,
    @Json(name = "id") val id: Int,
    @Json(name = "my_status") val status: String = ""
) {
    companion object {
        fun toUserModel(
            createAccountResDTO: CreateAccountResDTO,
            firebaseId: String,
            accessPin: String,
            status: String
        ): User {
            return User(
                firebaseId,
                createAccountResDTO.id,
                createAccountResDTO.nickname,
                createAccountResDTO.fullName,
                accessPin,
                "",
                status,
                "",
                ""
            )
        }
    }
}