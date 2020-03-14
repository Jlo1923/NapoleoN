package com.naposystems.pepito.dto.recoveryAccount

import com.naposystems.pepito.model.recoveryAccount.RecoveryAccountUserType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecoveryAccountUserTypeResDTO(
    @Json(name = "user_type") val userType: Int,
    @Json(name = "new_recovery_info") val newRecoveryInfo: List<RecoveryAccountResDTO>
) {
    companion object {
        fun toModel(response: RecoveryAccountUserTypeResDTO): RecoveryAccountUserType {
            return RecoveryAccountUserType(
                userType = response.userType,
                newRecoveryInfo = RecoveryAccountResDTO.toListRecoveryQuestions(response.newRecoveryInfo)
            )
        }
    }
}