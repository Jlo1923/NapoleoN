package com.naposystems.pepito.repository.accountAttackDialog

import com.naposystems.pepito.dto.accountAttackDialog.AccountAttackDialogReqDTO
import com.naposystems.pepito.dto.accountAttackDialog.AccountAttackDialogResDTO
import com.naposystems.pepito.ui.accountAttack.IContractAccountAttackDialog
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class AccountAttackDialogRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
): IContractAccountAttackDialog.Repository {

    override suspend fun blockAttack(): Response<AccountAttackDialogResDTO> {
        val attack = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_ATTCKER_ID, ""
        )

        return napoleonApi.blockAttacker(AccountAttackDialogReqDTO(attack))
    }

    override suspend fun resetExistingAttack() {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_EXISTING_ATTACK,
            Constants.ExistingAttack.NOT_EXISTING.type
        )
    }
}