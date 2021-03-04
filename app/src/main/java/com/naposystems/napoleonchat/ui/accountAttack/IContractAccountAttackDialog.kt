package com.naposystems.napoleonchat.ui.accountAttack

import com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog.AccountAttackDialogResDTO
import retrofit2.Response

interface IContractAccountAttackDialog {
    interface ViewModel {
        fun blockAttack()
        fun resetExistingAttack()
    }

    interface Repository {
        suspend fun blockAttack(): Response<AccountAttackDialogResDTO>
        suspend fun resetExistingAttack()
    }
}