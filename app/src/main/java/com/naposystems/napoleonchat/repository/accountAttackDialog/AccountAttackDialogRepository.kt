package com.naposystems.napoleonchat.repository.accountAttackDialog

import com.naposystems.napoleonchat.dto.accountAttackDialog.AccountAttackDialogReqDTO
import com.naposystems.napoleonchat.dto.accountAttackDialog.AccountAttackDialogResDTO
import com.naposystems.napoleonchat.ui.accountAttack.IContractAccountAttackDialog
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class AccountAttackDialogRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
): IContractAccountAttackDialog.Repository {

    override suspend fun blockAttack(): Response<AccountAttackDialogResDTO> {
        val attack = sharedPreferencesManager.getString(
            Constants.SharedPreferences.PREF_ATTACKER_ID, ""
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