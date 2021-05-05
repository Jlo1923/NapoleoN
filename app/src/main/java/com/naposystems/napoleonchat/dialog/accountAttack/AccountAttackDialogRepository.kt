package com.naposystems.napoleonchat.dialog.accountAttack

import com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog.AccountAttackDialogResDTO
import retrofit2.Response

interface AccountAttackDialogRepository {
    suspend fun blockAttack(): Response<AccountAttackDialogResDTO>
    suspend fun resetExistingAttack()
}