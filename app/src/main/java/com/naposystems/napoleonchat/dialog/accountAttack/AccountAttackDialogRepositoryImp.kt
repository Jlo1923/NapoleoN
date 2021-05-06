package com.naposystems.napoleonchat.dialog.accountAttack

import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog.AccountAttackDialogReqDTO
import com.naposystems.napoleonchat.source.remote.dto.accountAttackDialog.AccountAttackDialogResDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import retrofit2.Response
import javax.inject.Inject

class AccountAttackDialogRepositoryImp
@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val napoleonApi: NapoleonApi
) : AccountAttackDialogRepository {

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