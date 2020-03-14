package com.naposystems.pepito.di.modules

import com.naposystems.pepito.repository.accountAttackDialog.AccountAttackDialogRepository
import com.naposystems.pepito.ui.accountAttack.IContractAccountAttackDialog
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AccountAttackDialogModule {

    @Provides
    @Singleton
    fun provideRepository(
        sharedPreferencesManager: SharedPreferencesManager,
        napoleonApi: NapoleonApi
    ): IContractAccountAttackDialog.Repository {
        return AccountAttackDialogRepository(sharedPreferencesManager, napoleonApi)
    }
}